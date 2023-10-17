import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  `jvm-test-suite`
  jacoco
  alias(libs.plugins.kotlin)
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

repositories { mavenCentral() }

dependencies {
  api(project(":lib"))
  implementation(libs.ztzip)
  implementation(libs.proguard)
}

// val libProject = project(":lib")
//
// val fromLib: Provider<List<File>> =
//    configurations.runtimeClasspath.map {
//      val firstLevel = it.resolvedConfiguration.firstLevelModuleDependencies
//
//      firstLevel
//          .first { it.moduleGroup == libProject.group && it.moduleName == libProject.name }
//          .allModuleArtifacts
//          .map { it.file }
//    }

val generateSerializatorBuildInfo =
    tasks.register<GenerateSerializatorBuildInfo>("generateSerializatorBuildInfo") {
      dependsOn(":lib:jar")
      dependsOn(":lib-kotlin:jar")
      dependencies.set(configurations.named("testExamplesRuntimeClasspath"))
      output.set(layout.buildDirectory.dir("generated/sources/build_info"))
    }

project.tasks.named("compileJava") { mustRunAfter(generateSerializatorBuildInfo) }

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

mavenPublishing {
  coordinates(mvnGroupId, mvnArtifactId, mvnVersion)

  pom {
    licenses {
      license {
        name = "The GNU General Public License v3.0"
        url = "https://www.gnu.org/licenses/gpl-3.0.txt"
        distribution = "https://www.gnu.org/licenses/gpl-3.0.txt"
      }
    }
  }
}

@Suppress("UnstableApiUsage")
testing {
  suites {
    val test by getting(JvmTestSuite::class)

    val testExamples =
        register<JvmTestSuite>("testExamples") {
          dependencies {
            implementation(project(":lib-kotlin"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
          }
          targets {
            all {
              testTask.configure {
                minHeapSize = "512m"
                maxHeapSize = "1024m"
                shouldRunAfter(":lib:publishToMavenLocal")
                shouldRunAfter(test)
              }
            }
          }
        }

    getByName<JvmTestSuite>("test") {
      targets { all { testTask.configure { dependsOn(testExamples) } } }
    }

    withType<JvmTestSuite> {
      targets {
        all {
          testTask.configure {
            testLogging { events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED) }
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
      sources { java { srcDirs(generateSerializatorBuildInfo) } }
    }
  }
}

abstract class GenerateSerializatorBuildInfo : DefaultTask() {
  @get:InputFiles abstract val dependencies: ListProperty<File>

  @get:InputFiles abstract val supportLib: ListProperty<File>

  @get:OutputDirectory abstract val output: DirectoryProperty

  @TaskAction
  fun invoke() {
    val dir = outputs.files.single().toPath()

    val clz =
        """
                package pl.andrzejressel.deeplambdaserialization.serializator;

                import java.nio.file.Path;
                import java.nio.file.Paths;

                public class BuildInfo {
                    public static final Path location = Paths.get("${project.projectDir.toString().replace("\\","\\\\")}");
                    public static final String dependencies = "${(dependencies.get() - supportLib.get().toSet()).joinToString(
                ",",
            ).replace("\\","\\\\")}";
                    public static final String supportLib = "${supportLib.get().joinToString(",").replace("\\","\\\\")}";
                }
            """
            .trimIndent()

    dir.resolve("pl/andrzejressel/deeplambdaserialization/serializator")
        .createDirectories()
        .resolve("BuildInfo.java")
        .writeText(clz)
  }
}

tasks.jacocoTestReport {
  dependsOn("test", "testExamples")

  executionData.setFrom(fileTree(layout.buildDirectory).include("/jacoco/*.exec"))
  reports {
    xml.required = true
    html.required = true
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }
