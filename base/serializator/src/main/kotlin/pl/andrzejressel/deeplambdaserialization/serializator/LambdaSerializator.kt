// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.serializator

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import org.zeroturnaround.zip.ZipUtil
import pl.andrzejressel.deeplambdaserialization.lib.*
import proguard.Configuration
import proguard.ConfigurationParser
import proguard.ProGuard
import proguard.classfile.AccessConstants
import proguard.classfile.ClassPool
import proguard.classfile.Clazz
import proguard.classfile.util.ClassSuperHierarchyInitializer
import proguard.classfile.util.WarningPrinter
import proguard.classfile.visitor.ClassPoolFiller
import proguard.io.*

class LambdaSerializator(
    private val dependencies: Set<Path>,
    private val supportLib: Set<Path>,
    classes: Set<Path>,
    private val output: Path,
    private val tmpDirectory: Path,
    private val additionalProguardOptions: List<String>
) {

  private val programClassPool: ClassPool
  private val serializableFunction: Clazz
  private val classesDir: Path

  init {
    output.createDirectories()
    tmpDirectory.createDirectories()

    dependencies.intersect(supportLib).run {
      if (isNotEmpty()) {
        throw IllegalArgumentException("dependencies and supportLib are sharing elements: $this")
      }
    }

    val programClassPool = createProgramClassPool(classes)
    val libraryClassPool = createLibraryClassPool()

    initializeClassPools(programClassPool, libraryClassPool)
    programClassPool.classesAccept(MakeEverythingPublic())

    val programClasses = tmpDirectory.resolve("program_classes").toFile()
    programClassPool.classesAccept(DataEntryClassWriter(DirectoryWriter(programClasses)))
    this.classesDir = programClasses.toPath()

    serializableFunction =
        libraryClassPool.getClass(SerializableFunction::class.java.name.replace('.', '/'))
            ?: programClassPool.getClass(SerializableFunction::class.java.name.replace('.', '/'))
            ?: throw RuntimeException("Cannot find SerializableFunction")
    this.programClassPool = programClassPool
  }

  fun getClasses(): List<ClassName> {
    return programClassPool
        .classes()
        .filter { it.extendsOrImplements(serializableFunction) }
        .filterNot { it.accessFlags and AccessConstants.ABSTRACT == AccessConstants.ABSTRACT }
        .map { ProguardClassName(it.name) }
  }

  fun createJar(className: ClassName): File {
    val base64lambdaClassName = NameUtils.getJarName(className)

    println("PROGUARD START: ${Date()}")

    val outputFile = tmpDirectory.resolve("${base64lambdaClassName}.step1.jar").toFile()
    outputFile.parentFile.toPath().createDirectories()

    val injars = buildList {
      (dependencies - supportLib)
          .filter { it.exists() }
          .forEach { dep -> add(dep.toFile().absolutePath) }
      add(classesDir.absolutePathString())
    }
    val outjars = listOf(outputFile.absolutePath)
    val libraryJars =
        buildList {
              add(File("${System.getProperty("java.home")}/jmods/java.base.jmod"))
              supportLib.forEach { add(it.toFile()) }
            }
            .map { it.absolutePath }

    val configurationString =
        """
            -keep class ${className.javaClassName} {*;}
            ${additionalProguardOptions.joinToString(separator = "\n")}
            ${injars.joinToString(separator = "\n") { "-injars $it" }}
            ${outjars.joinToString(separator = "\n") { "-outjars $it" }}
            ${libraryJars.joinToString(separator = "\n") { "-libraryjars $it" }}
            -dontwarn **
            -dontoptimize
            -dontobfuscate
            -forceprocessing
        """
            .trimIndent()
    //            -keep public interface com.amazonaws.services.lambda.runtime.RequestHandler {*;}
    //            -keep public class Handler {*;}

    // Create the default options.
    val configuration = Configuration()
    ConfigurationParser(configurationString, "", File("."), Properties()).parse(configuration)

    // Execute ProGuard with these options.
    ProGuard(configuration).execute()

    val entriesToRemove = mutableListOf<String>()

    ZipUtil.iterate(outputFile) { _, zipEntry ->
      if (zipEntry.name.endsWith("kotlin_metadata") || zipEntry.name.endsWith("kotlin_buildins")) {
        entriesToRemove.add(zipEntry.name)
      }
    }

    ZipUtil.removeEntries(outputFile, entriesToRemove.toTypedArray())

    println("PROGUARD END: ${Date()}")

    val file = LambdaInnerClassFixer.run(outputFile, supportLib, className)
    val destFile = output.resolve(file.name)

    Files.copy(file.toPath(), destFile, REPLACE_EXISTING)

    return destFile.toFile()
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
        ClassSuperHierarchyInitializer(
            programClassPool, libraryClassPool, myLogger, myLogger
            //            WarningPrinter(PrintWriter(System.err)),
            //            WarningPrinter(PrintWriter(System.err))
            )
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

  private fun createLibraryClassPool(): ClassPool {
    val libraryClassPool = ClassPool()
    val baseDataEntryReader =
        ClassFilter(ClassReader(true, true, true, false, null, ClassPoolFiller(libraryClassPool)))

    (dependencies + supportLib)
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
