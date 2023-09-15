package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import pl.andrzejressel.deeplambdaserialization.lib.BuildInfo

@Suppress("unused")
class DeepSerializationPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val impl = try {
            project.configurations.getAt("implementation")
        } catch (e: Exception) {
            project.logger.error("Cannot find 'implementation' configuration. Probably java plugin is not applied")
            throw e
        }

//        val dependency = project.dependencies.create("pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
//        project.dependencies.add("implementation", dependency)


//        project.dependencies.r

//        project.configurations.filter { it.isCanBeResolved }.forEach {
//            println(it.name)
//        }

//        impl.resolvedConfiguration.resolvedArtifacts.map {
//            println(it)
//        }

        project.dependencies {
            add("implementation", "pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
        }


        val ext = project.extensions.create<DeepSerializationPluginExtension>("deepSerializationPluginExtension")

        project.tasks.register<GenerateLambdaJars>("deeplambdaserialization") {
            dependencies.set(ext.dependencies)
            classes.set(ext.dependencies)
            output.set(ext.output)
        }

    //        project.tasks.register("deeplambdaserialization") {
//            group = "deeplambdaserialization"
//            doLast {
//                println("Hello from plugin 'pl.andrzejressel.deeplambdaserialization'")
//            }
//        }
    }
}
