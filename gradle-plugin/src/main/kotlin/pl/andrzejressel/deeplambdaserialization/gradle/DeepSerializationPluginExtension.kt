package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

interface DeepSerializationPluginExtension {
  @get:Input val availableModules: ListProperty<SimpleModule>
  @get:Input val additionalProguardOptions: ListProperty<String>

  fun addProject(project: Project) {
    val group = project.group.toString()
    val name = project.name

    availableModules.add(SimpleModule(group, name))
  }

  fun addKeep(additionalKeep: String) {
    additionalProguardOptions.add("-keep $additionalKeep")
  }
}
