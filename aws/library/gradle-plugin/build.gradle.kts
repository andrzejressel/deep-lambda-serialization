import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.api.tasks.testing.logging.TestLogEvent
import pl.andrzejressel.deeplambdaserialization.buildplugin.ChildPlugin.Companion.License
import pl.andrzejressel.deeplambdaserialization.buildplugin.ChildPlugin.Companion.childSetup

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  `jvm-test-suite`
  jacoco
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

childSetup(License.GPL)

configurations { create("customCompileOnly") { isTransitive = true } }

dependencies {
  add("customCompileOnly", "pl.andrzejressel.deeplambdaserialization:gradle-plugin:$version")
  compileOnly("pl.andrzejressel.deeplambdaserialization:gradle-plugin:$version")
}

tasks.test {
  useJUnitPlatform()
  testLogging { events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED) }
}

gradlePlugin {
  val deeplambdaserializationaws by
      plugins.creating {
        version = project.version
        id = "pl.andrzejressel.deeplambdaserialization.aws"
        implementationClass =
            "pl.andrzejressel.deeplambdaserialization.aws.gradle.DeepSerializationAWSPlugin"
        tags.add("serialization")
      }
}

val generateBuildInfo by
    tasks.registering {
      inputs.property("version", parent!!.version)
      outputs.cacheIf { true }
      outputs.dir(layout.buildDirectory.dir("generated/sources/build_info"))
      doLast {
        val dir = outputs.files.single().toPath()

        val clz =
            """
            package pl.andrzejressel.deeplambdaserialization.aws.lib;
            
            public class BuildInfo {
              public static String version = "${this.inputs.properties["version"]}";
            }
            """
                .trimIndent()

        dir.resolve("pl/andrzejressel/deeplambdaserialization/aws/lib")
            .createDirectories()
            .resolve("BuildInfo.java")
            .writeText(clz)
      }
    }

sourceSets { main { java { srcDirs(generateBuildInfo) } } }

@Suppress("UnstableApiUsage")
testing {
  suites {
    register<JvmTestSuite>("integrationTest") {
      dependencies {
        implementation(gradleTestKit())
        implementation(libs.ztzip)
      }

      gradlePlugin.testSourceSets(this.sources)

      targets { all { testTask.configure { dependsOn(":handler:publishToMavenLocal") } } }
    }

    withType<JvmTestSuite> {
      targets { all { testTask.configure { maxHeapSize = "4g" } } }
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

@Suppress("UnstableApiUsage")
tasks.named("check") {
  dependsOn(
      testing.suites.named("integrationTest"),
  )
}

tasks.jacocoTestReport {
  dependsOn("test", "integrationTest")

  executionData.setFrom(fileTree(layout.buildDirectory).include("/jacoco/*.exec"))
  reports {
    xml.required = true
    html.required = true
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }

val additionalDeps =
    configurations.named<Configuration>("customCompileOnly").map { it.resolvedConfiguration.files }

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
  this.pluginClasspath.from(additionalDeps)
}
