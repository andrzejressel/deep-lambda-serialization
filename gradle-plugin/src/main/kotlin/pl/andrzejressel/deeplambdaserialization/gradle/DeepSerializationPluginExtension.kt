package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import java.io.File

interface DeepSerializationPluginExtension {
    @get:InputFiles
    val dependencies: ListProperty<File>
    @get:InputFiles
    val classes: ListProperty<File>

    fun addProject(project: Project) {

    }

}