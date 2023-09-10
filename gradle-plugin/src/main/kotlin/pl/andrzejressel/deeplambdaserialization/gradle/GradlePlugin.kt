package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import pl.andrzejressel.deeplambdaserialization.lib.BuildInfo

@Suppress("unused")
class GradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {

        try {
            project.configurations.getAt("implementation")
        } catch (e: Exception) {
            project.logger.error("Cannot find 'implementation' configuration. Probably java plugin is not applied")
            throw e
        }

//        val dependency = project.dependencies.create("pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
//        project.dependencies.add("implementation", dependency)

        project.dependencies {
            add("implementation", "pl.andrzejressel.deeplambdaserialization:lib:${BuildInfo.version}")
        }
//        project.tasks.register("deeplambdaserialization") {
//            group = "deeplambdaserialization"
//            doLast { println("Hello from plugin 'pl.andrzejressel.deeplambdaserialization'") }
//        }
    }
}
