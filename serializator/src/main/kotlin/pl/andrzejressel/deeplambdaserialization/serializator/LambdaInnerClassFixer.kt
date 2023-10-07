package pl.andrzejressel.deeplambdaserialization.serializator

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import pl.andrzejressel.deeplambdaserialization.lib.*
import proguard.classfile.*
import proguard.classfile.editor.ClassBuilder
import proguard.classfile.util.ClassSuperHierarchyInitializer
import proguard.classfile.util.WarningPrinter
import proguard.classfile.visitor.ClassPoolFiller
import proguard.io.*
import proguard.io.util.IOUtil

object LambdaInnerClassFixer {

  fun run(outputFile: File, supportLib: Set<Path>, className: ClassName): File {
    val getJarName = NameUtils.getJarName(className)
    val newOutputFile = outputFile.toPath().parent.resolve("${getJarName}.jar")

    val programClassPool = createProgramClassPool(setOf(outputFile.toPath()))
    val libraryClassPool = createLibraryClassPool(supportLib)

    initializeClassPools(programClassPool, libraryClassPool)

    val serializableFunction =
        libraryClassPool.getClass(SerializableFunctionN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(SerializableFunctionN::class.java.name.replace('.', '/'))
                ?: throw RuntimeException("Cannot find SerializableFunctionN")

    val serializableInputFunction =
        libraryClassPool.getClass(SerializableInputFunctionN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableInputFunctionN::class.java.name.replace('.', '/'))

    val serializableInputOutputFunction =
        libraryClassPool.getClass(
            SerializableInputOutputFunctionN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableInputOutputFunctionN::class.java.name.replace('.', '/'))

    val clz = programClassPool.getClass(className.proguardClassName) as ProgramClass
    val parentClzBase = clz.superClass

    val parentClzInit =
        when (parentClzBase) {
          is ProgramClass -> parentClzBase.methods.first { it.getName(parentClzBase) == "<init>" }
          is LibraryClass -> parentClzBase.methods.first { it.getName(parentClzBase) == "<init>" }
          else ->
              throw IllegalStateException(
                  "parentClzBase has invalid class: ${parentClzBase.javaClass}")
        }

    val cb =
        if (clz.findMethod("<init>", "()V") == null) {
          ClassBuilder(clz)
              .addMethod(AccessConstants.PUBLIC, "<init>", "()V", 50) { code ->
                code.aload_0().invokespecial(parentClzBase, parentClzInit).return_()
              }
              .programClass
        } else {
          clz
        }

    val programClassBuilder =
        ClassBuilder(
            VersionConstants.CLASS_VERSION_1_8,
            AccessConstants.PUBLIC,
            "EntryPoint",
            ClassConstants.NAME_JAVA_LANG_OBJECT)

    programClassBuilder.addMethod(
        AccessConstants.PUBLIC or AccessConstants.STATIC,
        "execute",
        "([Ljava/lang/Object;)Ljava/lang/Object;",
        50) { code ->
          val method =
              serializableFunction.findMethod("execute", "([Ljava/lang/Object;)Ljava/lang/Object;")

          code
              .new_(cb)
              .dup()
              .invokespecial(cb, cb.findMethod("<init>", "()V"))
              .aload_0()
              .invokevirtual(serializableFunction, method)
              .areturn()
        }

    if (serializableInputFunction != null && clz.extendsOrImplements(serializableInputFunction)) {
      val clz = serializableInputFunction
      val method = clz.findMethod("execute", "([[B)Ljava/lang/Object;")

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "execute",
          method.getDescriptor(clz),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(clz, method)
                .areturn()
          }
    }

    if (serializableInputOutputFunction != null &&
        clz.extendsOrImplements(serializableInputOutputFunction)) {

      val clz = serializableInputOutputFunction
      val objectMethod = clz.findMethod("executeAndSerialize", "([Ljava/lang/Object;)[B")
      val byteArrayMethod = clz.findMethod("executeAndSerialize", "([[B)[B")

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "executeAndSerialize",
          objectMethod.getDescriptor(clz),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(clz, objectMethod)
                .areturn()
          }

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "executeAndSerialize",
          byteArrayMethod.getDescriptor(clz),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(clz, byteArrayMethod)
                .areturn()
          }
    }

    programClassPool.addClass(cb)
    programClassPool.addClass(programClassBuilder.programClass)
    initializeClassPools(programClassPool, libraryClassPool)

    IOUtil.writeJar(programClassPool, newOutputFile.absolutePathString())

    return newOutputFile.toFile()
  }

  private fun initializeClassPools(programClassPool: ClassPool, libraryClassPool: ClassPool) {

    // TODO: Pipe to debug
    val myLogger =
        object : WarningPrinter(null) {
          override fun note(className: String?, message: String?) {}

          override fun note(className1: String?, className2: String?, message: String?) {}

          override fun print(className: String?, warning: String?) {}

          override fun print(className1: String?, className2: String?, warning: String?) {}
        }

    val hierarchyInit =
        ClassSuperHierarchyInitializer(programClassPool, libraryClassPool, myLogger, myLogger)
    programClassPool.classesAccept(hierarchyInit)
    libraryClassPool.classesAccept(hierarchyInit)
  }

  private fun createProgramClassPool(classes: Set<Path>): ClassPool {
    val programClassPool = ClassPool()
    classes
        .filter { it.exists() }
        .forEach { fileName ->
          val baseDataEntryReader =
              ClassFilter(
                  ClassReader(false, false, false, false, null, ClassPoolFiller(programClassPool)))

          if (fileName.isDirectory()) {
            DirectorySource(fileName.toFile()).pumpDataEntries(baseDataEntryReader)
          } else {
            FileSource(fileName.toFile()).pumpDataEntries(JarReader(baseDataEntryReader))
          }
        }
    return programClassPool
  }

  private fun createLibraryClassPool(supportLib: Set<Path>): ClassPool {
    val libraryClassPool = ClassPool()
    val baseDataEntryReader =
        ClassFilter(ClassReader(true, true, true, false, null, ClassPoolFiller(libraryClassPool)))

    (supportLib)
        .filter { it.exists() }
        .forEach { fileName ->
          if (fileName.isDirectory()) {
            DirectorySource(fileName.toFile()).pumpDataEntries(baseDataEntryReader)
          } else {
            FileSource(fileName.toFile()).pumpDataEntries(JarReader(baseDataEntryReader))
          }
        }

    FileSource(File("${System.getProperty("java.home")}/jmods/java.base.jmod"))
        .pumpDataEntries(JarReader(true, baseDataEntryReader))

    return libraryClassPool
  }
}
