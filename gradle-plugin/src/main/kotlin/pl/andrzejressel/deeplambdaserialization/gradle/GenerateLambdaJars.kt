package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.zeroturnaround.zip.ZipUtil
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunctionN
import proguard.*
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.classfile.ClassPool
import proguard.classfile.util.ClassSuperHierarchyInitializer
import proguard.classfile.util.WarningPrinter
import proguard.classfile.visitor.ClassPoolFiller
import proguard.io.*
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.io.path.createDirectories

abstract class GenerateLambdaJars : DefaultTask() {

    @get:InputFiles
    abstract val dependencies: ListProperty<File>
    @get:InputFiles
    abstract val classes: ListProperty<File>
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun generate() {
        val myTxt = output.get().asFile.toPath().createDirectories()

        val programClassPool = createProgramClassPool()
        val libraryClassPool = createLibraryClassPool()

        initializeClassPools(programClassPool, libraryClassPool)

        val serializableFunction2 =
            libraryClassPool.getClass(SerializableFunctionN::class.java.name.replace('.', '/'))
                ?: throw RuntimeException("Cannot find SerializableFunctionN in libraryClassPool")

        val myClz = programClassPool.getClass("pl/andrzejressel/lambdaprepared/app/java/MainJava$1")

        println(myClz.superClass?.superClass)

//        programClassPool.classes().filter { it.extendsOrImplements(serializableFunction2) }.forEach { it.accept(ClassPrinter()) }
//        programClassPool.classes().filter { it.extendsOrImplements(serializableFunction2) }.forEach { it.accept(DataEntryClassWriter(DirectoryWriter(myTxt.resolve(it.hashCode().toString()).toFile()))) }

//        Files.writeString(mapping1, programClassPool.classes().filter { it.extendsOrImplements(serializableFunction2) }.toString())

        programClassPool.classes().filter { it.extends_(serializableFunction2) }.forEach {
            val name = it.name
            logger.info("Packaging $name")
            createJar(name)
        }

    }

    private fun createJar(lambdaClassName: String) {
        val base64lambdaClassName = Base64.getEncoder().encodeToString(lambdaClassName.encodeToByteArray())

        val outputFile = output.get().asFile.toPath().resolve("${base64lambdaClassName}.jar").toFile()
        val memberSpecificationList = listOf(
            MemberSpecification(),
            MemberSpecification(2, 0, null, null, null),
            MemberSpecification(4, 0, null, null, null)
        )
        val configuration = Configuration().apply {
            programJars = ClassPath().apply {
                dependencies.get().filter { it.exists() }.forEach { dep ->
                    add(ClassPathEntry(dep, false))
                }
                classes.get().filter { it.exists() }.forEach { dep ->
                    add(ClassPathEntry(dep, false))
                }
                add(ClassPathEntry(outputFile, true))
            }
            libraryJars = ClassPath().apply {
                add(ClassPathEntry(File("${System.getProperty("java.home")}/jmods/java.base.jmod"), false))
            }
            obfuscate = false
    //            optimize = false
            keep = buildList {
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
                            "pl/andrzejressel/lambdaprepared/app/java/Keep",
                            null,
                            null,
                            memberSpecificationList,
                            memberSpecificationList
                        )
                    )
                )
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
                            lambdaClassName,
                            null,
                            null,
                            memberSpecificationList,
                            memberSpecificationList
                        )
                    )
                )
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
    }

    private fun initializeClassPools(programClassPool: ClassPool, libraryClassPool: ClassPool) {

        //TODO: Pipe to debug
        val myLogger = object: WarningPrinter(null) {
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

    private fun createProgramClassPool(): ClassPool {
        val programClassPool = ClassPool()
        classes.get().filter { it.exists() }.forEach { fileName ->

            val baseDataEntryReader = ClassFilter(
                ClassReader(
                    false, false, false, false, null,
                    ClassPoolFiller(programClassPool)
                )
            )

            if (fileName.isDirectory) {
                DirectorySource(fileName).pumpDataEntries(baseDataEntryReader)
            } else {
                FileSource(fileName).pumpDataEntries(JarReader(baseDataEntryReader))
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

        dependencies.get().filter { it.exists() }.forEach { fileName ->

            if (fileName.isDirectory) {
                DirectorySource(fileName).pumpDataEntries(baseDataEntryReader)
            } else {
                FileSource(fileName).pumpDataEntries(JarReader(baseDataEntryReader))
            }
        }

        FileSource(File("${System.getProperty("java.home")}/jmods/java.base.jmod")).pumpDataEntries(JarReader(true, baseDataEntryReader))

        return libraryClassPool
    }

}