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
    jacoco
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

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = mvnGroupId
            artifactId = mvnArtifactId
            version = mvnVersion
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/andrzejressel/deep-java-code-serialization")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}


@Suppress("UnstableApiUsage")
tasks.named("check") {
    dependsOn(
        testing.suites.named("integrationTest")
    )
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = false
    }
}

tasks.named("check") {
    dependsOn("jacocoTestReport")
}