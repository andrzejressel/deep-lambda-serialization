package pl.andrzejressel.deeplambdaserialization.serializator

import org.zeroturnaround.zip.ZipUtil
import pl.andrzejressel.deeplambdaserialization.lib.ClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils
import pl.andrzejressel.deeplambdaserialization.lib.ProguardClassName
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunctionN
import proguard.*
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.classfile.ClassPool
import proguard.classfile.Clazz
import proguard.classfile.util.ClassSuperHierarchyInitializer
import proguard.classfile.util.WarningPrinter
import proguard.classfile.visitor.ClassPoolFiller
import proguard.io.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


class LambdaSerializator(private val dependencies: Set<Path>, private val supportLib: Set<Path>, classes: Set<Path>, private val output: Path) {

    private val programClassPool: ClassPool
    private val serializableFunction: Clazz
    private val classesDir: Path

    init {
        output.createDirectories()

        dependencies.intersect(supportLib).run {
            if (isNotEmpty()) {
                throw IllegalArgumentException("dependencies and supportLib are sharing elements: $this")
            }
        }

        val programClassPool = createProgramClassPool(classes)
        val libraryClassPool = createLibraryClassPool()

        initializeClassPools(programClassPool, libraryClassPool)
        programClassPool.classesAccept(MakeEverythingPublic())

        val programClasses = output.resolve("program_classes").toFile()
        programClassPool.classesAccept(DataEntryClassWriter(DirectoryWriter(programClasses)))
        this.classesDir = programClasses.toPath()

        serializableFunction =
            libraryClassPool.getClass(SerializableFunctionN::class.java.name.replace('.', '/'))
                ?: throw RuntimeException("Cannot find SerializableFunctionN in libraryClassPool")
        this.programClassPool = programClassPool
    }

    fun getClasses(): List<ClassName> {
        return programClassPool.classes().filter { it.extendsOrImplements(serializableFunction) }
            .map { ProguardClassName(it.name) }
    }

    fun createJar(className: ClassName): File {
        val base64lambdaClassName = NameUtils.getJarName(className)

        val outputFile = output.resolve("${base64lambdaClassName}.jar").toFile()
        val memberSpecificationList = listOf(
            MemberSpecification(),
            MemberSpecification(2, 0, null, null, null),
            MemberSpecification(4, 0, null, null, null)
        )
        val configuration = Configuration().apply {
            programJars = ClassPath().apply {
                (dependencies - supportLib).filter { it.exists() }.forEach { dep ->
                    add(ClassPathEntry(dep.toFile(), false))
                }
//                classes.filter { it.exists() }.forEach { dep ->
                add(ClassPathEntry(classesDir.toFile(), false))
//                }
                add(ClassPathEntry(outputFile, true))
            }
            warn = listOf("**", "org.gradle.internal.impldep.**", "org/gradle/internal/impldep/**", "module-info")
            libraryJars = ClassPath().apply {
                add(ClassPathEntry(File("${System.getProperty("java.home")}/jmods/java.base.jmod"), false))
                supportLib.forEach {
                    add(ClassPathEntry(it.toFile(), false))
                }
//                add(ClassPathEntry(supportLib.toFile(), false))
            }
            //TODO: Replace with shadowing
            optimize = false
            obfuscate = false
//            keepAttributes =
//                """
//                    Exceptions,InnerClasses,Signature,Deprecated,
//                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Synthetic,LocalVariable*,Runtime*,MethodParameters
//                """.trimIndent().split(',').toList()

            //            optimize = false
            keep = buildList {
/*                add(
                    KeepClassSpecification(
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        null,
                        ClassSpecification(
                            null,
                            0,
                            0,
                            null,
                            "pl/andrzejressel/lambdaprepared/app/java/Keep",
                            null,
                            null,
                            memberSpecificationList,
                            memberSpecificationList
                        )
                    )
                )*/
                add(
                    KeepClassSpecification(
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        null,
                        ClassSpecification(
                            null,
                            0,
                            0,
                            null,
                            className.proguardClassName,
                            null,
                            null,
                            memberSpecificationList,
                            memberSpecificationList
                        )
                    )
                )
//                add(
//                    KeepClassSpecification(
//                        true,
//                        true,
//                        false,
//                        false,
//                        false,
//                        false,
//                        false,
//                        false,
//                        null,
//                        ClassSpecification(
//                            null,
//                            0,
//                            0,
//                            null,
//                            "pl/andrzejressel/deeplambdaserialization/gradle/IntPair",
//                            null,
//                            null,
//                            memberSpecificationList,
//                            memberSpecificationList
//                        )
//                    )
//                )
            }

        }

        // Execute ProGuard with these options.
        ProGuard(configuration).execute()

        val entriesToRemove = mutableListOf<String>()

        ZipUtil.iterate(outputFile) { _, zipEntry ->
            if (zipEntry.name.endsWith("kotlin_metadata") || zipEntry.name.endsWith("kotlin_buildins")) {
                entriesToRemove.add(zipEntry.name)
            }
        }

        ZipUtil.removeEntries(outputFile, entriesToRemove.toTypedArray())
        return outputFile
    }

    private fun initializeClassPools(programClassPool: ClassPool, libraryClassPool: ClassPool) {

        //TODO: Pipe to debug
        val myLogger = object : WarningPrinter(null) {
            override fun note(className: String?, message: String?) {
            }

            override fun note(className1: String?, className2: String?, message: String?) {

            }

            override fun print(className: String?, warning: String?) {

            }

            override fun print(className1: String?, className2: String?, warning: String?) {

            }
        }

        val hierarchyInit = ClassSuperHierarchyInitializer(
            programClassPool,
            libraryClassPool,
            myLogger,
            myLogger
//            WarningPrinter(PrintWriter(System.err)),
//            WarningPrinter(PrintWriter(System.err))
        )
        programClassPool.classesAccept(hierarchyInit)
        libraryClassPool.classesAccept(hierarchyInit)
    }

    private fun createProgramClassPool(classes: Set<Path>): ClassPool {
        val programClassPool = ClassPool()
        classes.filter { it.exists() }.forEach { fileName ->

            val baseDataEntryReader = ClassFilter(
                ClassReader(
                    false, false, false, false, null,
                    ClassPoolFiller(programClassPool)
                )
            )

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
        val baseDataEntryReader = ClassFilter(
            ClassReader(
                true, true, true, false, null,
                ClassPoolFiller(libraryClassPool)
            )
        )

        (dependencies + supportLib).filter { it.exists() }.forEach { fileName ->

            if (fileName.isDirectory()) {
                DirectorySource(fileName.toFile()).pumpDataEntries(baseDataEntryReader)
            } else {
                FileSource(fileName.toFile()).pumpDataEntries(JarReader(baseDataEntryReader))
            }
        }

        FileSource(File("${System.getProperty("java.home")}/jmods/java.base.jmod")).pumpDataEntries(
            JarReader(
                true,
                baseDataEntryReader
            )
        )

        return libraryClassPool
    }

}