package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.*
import pl.andrzejressel.deeplambdaserialization.lib.BuildInfo

@Suppress("unused")
class DeepSerializationPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val implementation = try {
            project.configurations.getAt("implementation")
        } catch (e: Exception) {
            project.logger.error("Cannot find 'implementation' configuration. Probably java plugin is not applied")
            throw e
        }

//        val javaPlugin = project.convention.getPlugin<JavaPluginConvention>()
        val javaPlugin = project.extensions.getByType<JavaPluginExtension>()

//        val dependency = project.dependencies.create("pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
//        project.dependencies.add("implementation", dependency)


//        project.dependencies.r

//        project.configurations.filter { it.isCanBeResolved }.forEach {
//            println(it.name)
//        }

//        impl.resolvedConfiguration.resolvedArtifacts.map {
//            println(it)
//        }


        val classesTask = project.tasks.named("compileJava")
        val allClasses = project.configurations.named("runtimeClasspath")

        println(allClasses)

        project.dependencies {
            add(implementation.name, "pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
        }

        val ext = project.extensions.create<DeepSerializationPluginExtension>("deepSerializationPluginExtension")

        val jarDirs = project.layout.buildDirectory.dir("generated/deep_serializator_plugin_jars")

        val generateJars = project.tasks.register<GenerateLambdaJars>("deeplambdaserialization") {
            this.allClasses.set(allClasses)
            dependencies.set(ext.dependencies)
            classes.set(classesTask.map { it.outputs.files.filter { it.isDirectory || it.extension == "jar" } })
            output.set(jarDirs)
        }

        javaPlugin.sourceSets.getByName("main") {
            java {
                resources {
                    srcDirs(generateJars)
                }
            }
        }

    }
}
