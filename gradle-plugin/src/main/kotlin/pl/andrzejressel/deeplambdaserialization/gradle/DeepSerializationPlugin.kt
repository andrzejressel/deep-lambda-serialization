package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import pl.andrzejressel.deeplambdaserialization.lib.BuildInfo

@Suppress("unused")
class DeepSerializationPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    val implementation =
        try {
          project.configurations.getAt("implementation")
        } catch (e: UnknownConfigurationException) {
          project.logger.error(
              "Cannot find 'implementation' configuration. Probably java plugin is not applied")
          throw e
        }

    val javaPlugin = project.extensions.getByType<JavaPluginExtension>()

    val compileJavaTask = project.tasks.named("compileJava")
    val compileKotlinTask =
        try {
          project.tasks.named("compileKotlin")
        } catch (e: UnknownTaskException) {
          null
        }

    val allClasses = project.configurations.named("runtimeClasspath")

    project.dependencies {
      add(implementation.name, "pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
    }

    val ext =
        project.extensions.create<DeepSerializationPluginExtension>(
            "deepSerializationPluginExtension")

    val jarDirs = project.layout.buildDirectory.dir("generated/deep_serializator_plugin_jars")

    val dependencies =
        allClasses.zip(ext.availableModules) { configuration, pair ->
          val topLevelDeps = configuration.resolvedConfiguration.firstLevelModuleDependencies
          val moduleToIgnore = topLevelDeps.flatMap { getAllModules(it, pair) }.toSet()
          moduleToIgnore.flatMap { it.moduleArtifacts }.map { it.file }
        }

    val subprojectsJarProjects =
        allClasses.map { configuration ->
          configuration.resolvedConfiguration.resolvedArtifacts
              .map { it.id.componentIdentifier }
              .filterIsInstance<ProjectComponentIdentifier>()
              .map { it.projectPath }
              .map { "$it:jar" }
        }

    val generateJars =
        project.tasks.register<GenerateLambdaJars>("deeplambdaserialization") {
          this.allClasses.set(allClasses)
          this.dependencies.set(dependencies)
          classes.set(
              compileJavaTask.map {
                it.outputs.files.filter { it.isDirectory || it.extension == "jar" }
              })
          if (compileKotlinTask != null) {
            classes.addAll(
                compileKotlinTask.map {
                  it.outputs.files.filter { it.isDirectory || it.extension == "jar" }
                })
          }
          this.additionalProguardOptions.set(ext.additionalProguardOptions)
          output.set(jarDirs)
          dependsOn(subprojectsJarProjects)
        }

    javaPlugin.sourceSets.getByName("main") { java { resources { srcDirs(generateJars) } } }
  }

  private fun getAllModules(
      dep: ResolvedDependency,
      modules: List<SimpleModule>
  ): Set<ResolvedDependency> {
    val p = SimpleModule(dep.moduleGroup, dep.moduleName)
    return if (modules.contains(p)) {
      setOf(dep)
    } else {
      dep.children.flatMap { getAllModules(it, modules) }.toSet()
    }
  }
}
