package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.kotlin.dsl.support.delegates.ProjectDelegate
import java.io.File

interface DeepSerializationPluginExtension {
    @get:Input
    val subProjects: ListProperty<String>
    @get:Input
    val pairs: ListProperty<Pair<String, String>>
    @get:InputFiles
    val sharedDependencies: ListProperty<File>
    @get:InputFiles
    val classes: ListProperty<File>

    fun addProject(project: Project) {
        val group = project.group.toString()
        val name = project.name

        pairs.add(group to name)
        subProjects.add(project.path)
    }

}