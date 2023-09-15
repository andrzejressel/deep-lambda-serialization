package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import java.io.File

interface DeepSerializationPluginExtension {
    @get:InputFiles
    val dependencies: ListProperty<File>
    @get:InputFiles
    val classes: ListProperty<File>
    @get:OutputDirectory
    val output: DirectoryProperty
}