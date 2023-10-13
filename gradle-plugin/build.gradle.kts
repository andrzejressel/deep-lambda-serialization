import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-gradle-plugin`
  `kotlin-dsl`
  `jvm-test-suite`
  jacoco
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
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

@Suppress("UnstableApiUsage")
testing {
  suites {
    val test by getting(JvmTestSuite::class)

    register<JvmTestSuite>("integrationTest") {
      dependencies {
        implementation(gradleTestKit())
        implementation(libs.ztzip)
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

mavenPublishing {
  coordinates(mvnGroupId, mvnArtifactId, mvnVersion)

  pom {
    licenses {
      license {
        name = "Gnu Lesser General Public License"
        url = "http://www.gnu.org/licenses/lgpl.txt"
        distribution = "http://www.gnu.org/licenses/lgpl.txt"
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
  dependsOn("test", "integrationTest")

  executionData.setFrom(fileTree(layout.buildDirectory).include("/jacoco/*.exec"))
  reports {
    xml.required = true
    html.required = true
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }
