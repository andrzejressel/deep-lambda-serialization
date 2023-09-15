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

        project.configurations.getAt("runtimeClasspath")
            .resolvedConfiguration
            .firstLevelModuleDependencies
            .map {
                it.module
            }

        project.configurations.getAt("runtimeClasspath")
            .resolvedConfiguration
            .resolvedArtifacts
            .forEach {
                println(it)
            }



    }
}