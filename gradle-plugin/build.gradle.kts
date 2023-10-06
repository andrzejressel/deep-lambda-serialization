import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  `jvm-test-suite`
  alias(libs.plugins.kotlin)
  `maven-publish`
  jacoco
  alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

dependencies {
  implementation(project(":serializator"))
  implementation(libs.ztzip)
  implementation(libs.proguard)
}

tasks.test {
  useJUnitPlatform()
  testLogging { events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED) }
}

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

gradlePlugin {
  val deeplambdaserialization by
      plugins.creating {
        version = mvnVersion
        id = "pl.andrzejressel.deeplambdaserialization"
        implementationClass =
            "pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPlugin"
        tags.add("serialization")
      }
}

val generateIntegrationTestBuildInfo by
    tasks.registering {
      inputs.property("token", project.findProperty("gpr.token") as String)
      outputs.dir(layout.buildDirectory.dir("generated/sources/build_info"))
      doLast {
        val dir = outputs.files.single().toPath()

        val clz =
            """
            package pl.andrzejressel.deeplambdaserialization.gradle.integrationtest;
            
            public class BuildInfo {
                public static String token = "${this.inputs.properties["token"]}";
            }
            """
                .trimIndent()

        dir.resolve("pl/andrzejressel/deeplambdaserialization/gradle/integrationtest")
            .createDirectories()
            .resolve("BuildInfo.java")
            .writeText(clz)
      }
    }

@Suppress("UnstableApiUsage")
testing {
  suites {
    val test by getting(JvmTestSuite::class)

    register<JvmTestSuite>("integrationTest") {
      dependencies {
        implementation(gradleTestKit())
        implementation(libs.ztzip)
      }

      sources { java { srcDirs(generateIntegrationTestBuildInfo) } }

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
      targets { all { testTask.configure { maxHeapSize = "8g" } } }
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

publishing { repositories { mavenLocal() } }

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
      testing.suites.named("integrationTest"),
  )
}

tasks.jacocoTestReport {
  reports {
    xml.required = true
    html.required = false
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }
