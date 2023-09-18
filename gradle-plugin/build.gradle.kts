import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `jvm-test-suite`
    alias(libs.plugins.kotlin)
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":serializator"))
    implementation(libs.ztzip)
    implementation(libs.proguard)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

gradlePlugin {
    val deeplambdaserialization by plugins.creating {
        id = "pl.andrzejressel.deeplambdaserialization"
        implementationClass = "pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPlugin"
        tags.add("serialization")
    }
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class)

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(gradleTestKit())
            }

            gradlePlugin.testSourceSets(this.sources)

            targets {
                all {
                    testTask.configure {
                        dependsOn(":lib:publishToMavenLocal")
                        shouldRunAfter(test)
                    }
                }
            }
        }

        withType<JvmTestSuite> {
            targets {
                all {
                    testTask.configure {
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
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

@Suppress("UnstableApiUsage")
tasks.named("check") {
    dependsOn(
        testing.suites.named("integrationTest")
    )
}
