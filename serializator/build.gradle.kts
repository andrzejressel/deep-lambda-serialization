import org.gradle.api.tasks.testing.logging.TestLogEvent
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

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

val javaPlugin: JavaPluginExtension = project.extensions.getByType()

val generateSerializatorBuildInfo by tasks.registering {
    inputs.files(project.configurations.getAt("compileClasspath").files)
    outputs.dir(layout.buildDirectory.dir("generated/sources/build_info"))
    dependsOn(":lib:compileJava")
    doLast {
        val dir = outputs.files.single().toPath()

        val clz = """
                package pl.andrzejressel.deeplambdaserialization.serializator;

                import java.nio.file.Path;
                import java.nio.file.Paths;

                public class BuildInfo {
                    public static final Path location = Paths.get("${project.projectDir.toString().replace("\\","\\\\")}");
                    public static final String dependencies = "${inputs.files.map { it.toPath() }.joinToString(",").replace("\\","\\\\")}";
                }
            """.trimIndent()

        dir
            .resolve("pl/andrzejressel/deeplambdaserialization/serializator")
            .createDirectories()
            .resolve("BuildInfo.java")
            .writeText(clz)
    }
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
