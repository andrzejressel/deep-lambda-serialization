import org.gradle.api.tasks.testing.logging.TestLogEvent
import kotlin.io.path.*

plugins {
    java
    `jvm-test-suite`
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.ztzip)
    implementation(libs.proguard)
}


val javaPlugin = project.extensions.getByType<JavaPluginExtension>()

val libProject = project(":lib")

val fromLib: Provider<List<File>> = configurations.runtimeClasspath.map {
    val firstLevel = it.resolvedConfiguration
        .firstLevelModuleDependencies

    println(firstLevel)
    firstLevel.first {
        it.moduleGroup == "pl.andrzejressel.deeplambdaserialization" && it.moduleName == "lib"
    }.allModuleArtifacts.map { it.file }
}

val generateSerializatorBuildInfo = tasks.register<GenerateSerializatorBuildInfo>("generateSerializatorBuildInfo") {
    dependsOn(":lib:jar")
    dependencies.set(configurations.runtimeClasspath)
    supportLib.set(fromLib)
    output.set(layout.buildDirectory.dir("generated/sources/build_info"))
}

project.tasks.named("compileJava") {
    mustRunAfter(generateSerializatorBuildInfo)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class)

        val testExamples = register<JvmTestSuite>("testExamples") {
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(":lib:publishToMavenLocal")
                        shouldRunAfter(test)
                    }
                }
            }
        }

        getByName<JvmTestSuite>("test") {
            targets {
                all {
                    testTask.configure {
                        dependsOn(testExamples)
                    }
                }
            }
        }

        withType<JvmTestSuite> {
            targets {
                all {
                    testTask.configure {
                        testLogging {
                            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                        }
                        maxHeapSize = "8g"
                    }
                }
            }
            dependencies {
                implementation(project())
                implementation(project(":lib"))
                implementation(libs.assertj.core)
                implementation(libs.commons.lang3)
            }
            useJUnitJupiter()
            sources {
                java {
                    srcDirs(generateSerializatorBuildInfo)
                }
            }
        }
    }
}

abstract class GenerateSerializatorBuildInfo : DefaultTask() {

    @get:InputFiles
    abstract val dependencies: ListProperty<File>

    @get:InputFiles
    abstract val supportLib: ListProperty<File>

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun invoke() {

        val dir = outputs.files.single().toPath()

        val clz = """
                package pl.andrzejressel.deeplambdaserialization.serializator;

                import java.nio.file.Path;
                import java.nio.file.Paths;

                public class BuildInfo {
                    public static final Path location = Paths.get("${project.projectDir.toString().replace("\\","\\\\")}");
                    public static final String dependencies = "${(dependencies.get() - supportLib.get().toSet()).joinToString(",").replace("\\","\\\\")}";
                    public static final String supportLib = "${supportLib.get().joinToString(",").replace("\\","\\\\")}";
                }
            """.trimIndent()

        dir
            .resolve("pl/andrzejressel/deeplambdaserialization/serializator")
            .createDirectories()
            .resolve("BuildInfo.java")
            .writeText(clz)
    }

}
