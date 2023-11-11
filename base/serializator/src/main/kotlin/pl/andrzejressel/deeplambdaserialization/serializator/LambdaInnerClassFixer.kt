// SPDX-License-Identifier: GPL-2.0-or-later
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

    val serializableFunction: Clazz? =
        libraryClassPool.getClass(SerializableFunctionN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(SerializableFunctionN::class.java.name.replace('.', '/'))

    val serializableFunctionWithContext: Clazz? =
        libraryClassPool.getClass(
            SerializableFunctionWithContextN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableFunctionWithContextN::class.java.name.replace('.', '/'))

    val serializableInputFunction: Clazz? =
        libraryClassPool.getClass(SerializableInputFunctionN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableInputFunctionN::class.java.name.replace('.', '/'))

    val serializableInputFunctionWithContext: Clazz? =
        libraryClassPool.getClass(
            SerializableInputFunctionWithContextN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableInputFunctionWithContextN::class.java.name.replace('.', '/'))

    val serializableInputOutputFunction: Clazz? =
        libraryClassPool.getClass(
            SerializableInputOutputFunctionN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableInputOutputFunctionN::class.java.name.replace('.', '/'))

    val serializableInputOutputFunctionWithContext: Clazz? =
        libraryClassPool.getClass(
            SerializableInputOutputFunctionWithContextN::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(
                SerializableInputOutputFunctionWithContextN::class.java.name.replace('.', '/'))

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
            "pl/andrzejressel/deeplambdaserialization/entrypoint/EntryPoint",
            ClassConstants.NAME_JAVA_LANG_OBJECT)

    if (serializableFunction != null && clz.extendsOrImplements(serializableFunction)) {
      val method =
          serializableFunction.findMethod("execute", "([Ljava/lang/Object;)Ljava/lang/Object;")
      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "execute",
          method.getDescriptor(serializableFunction),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(serializableFunction, method)
                .areturn()
          }
    }

    if (serializableFunctionWithContext != null &&
        clz.extendsOrImplements(serializableFunctionWithContext)) {
      val method =
          serializableFunctionWithContext.findMethod(
              "execute", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;")
      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "execute",
          method.getDescriptor(serializableFunctionWithContext),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .aload_1()
                .invokevirtual(serializableFunctionWithContext, method)
                .areturn()
          }
    }

    if (serializableInputFunction != null && clz.extendsOrImplements(serializableInputFunction)) {
      val method = serializableInputFunction.findMethod("execute", "([[B)Ljava/lang/Object;")

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "execute",
          method.getDescriptor(serializableInputFunction),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(serializableInputFunction, method)
                .areturn()
          }
    }

    if (serializableInputFunctionWithContext != null &&
        clz.extendsOrImplements(serializableInputFunctionWithContext)) {
      val method =
          serializableInputFunctionWithContext.findMethod(
              "execute", "(Ljava/lang/Object;[[B)Ljava/lang/Object;")

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "execute",
          method.getDescriptor(serializableInputFunctionWithContext),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .aload_1()
                .invokevirtual(serializableInputFunctionWithContext, method)
                .areturn()
          }
    }

    if (serializableInputOutputFunction != null &&
        clz.extendsOrImplements(serializableInputOutputFunction)) {

      val objectMethod =
          serializableInputOutputFunction.findMethod(
              "executeAndSerialize", "([Ljava/lang/Object;)[B")
      val byteArrayMethod =
          serializableInputOutputFunction.findMethod("executeAndSerialize", "([[B)[B")

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "executeAndSerialize",
          objectMethod.getDescriptor(serializableInputOutputFunction),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(serializableInputOutputFunction, objectMethod)
                .areturn()
          }

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "executeAndSerialize",
          byteArrayMethod.getDescriptor(serializableInputOutputFunction),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .invokevirtual(serializableInputOutputFunction, byteArrayMethod)
                .areturn()
          }
    }

    if (serializableInputOutputFunctionWithContext != null &&
        clz.extendsOrImplements(serializableInputOutputFunctionWithContext)) {

      val objectMethod =
          serializableInputOutputFunctionWithContext.findMethod(
              "executeAndSerialize", "(Ljava/lang/Object;[Ljava/lang/Object;)[B")
      val byteArrayMethod =
          serializableInputOutputFunctionWithContext.findMethod(
              "executeAndSerialize", "(Ljava/lang/Object;[[B)[B")

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "executeAndSerialize",
          objectMethod.getDescriptor(serializableInputOutputFunctionWithContext),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .aload_1()
                .invokevirtual(serializableInputOutputFunctionWithContext, objectMethod)
                .areturn()
          }

      programClassBuilder.addMethod(
          AccessConstants.PUBLIC or AccessConstants.STATIC,
          "executeAndSerialize",
          byteArrayMethod.getDescriptor(serializableInputOutputFunctionWithContext),
          50) { code ->
            code
                .new_(cb)
                .dup()
                .invokespecial(cb, cb.findMethod("<init>", "()V"))
                .aload_0()
                .aload_1()
                .invokevirtual(serializableInputOutputFunctionWithContext, byteArrayMethod)
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
