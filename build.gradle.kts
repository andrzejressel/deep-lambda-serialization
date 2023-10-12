import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure

group = "pl.andrzejressel.deeplambdaserialization"

plugins {
  alias(libs.plugins.git.version)
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish) apply false
}

val versionDetails: Closure<VersionDetails> by extra
val details = versionDetails()

version =
    if (details.isCleanTag) {
      val lastTag = details.lastTag
      if (lastTag.startsWith("v")) {
        // Release
        details.lastTag.removePrefix("v")
      } else {
        // main
        "main-SNAPSHOT"
      }
    } else {
      "DEV-SNAPSHOT"
    }

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlinGradle {
    target("*.gradle.kts") // default target for kotlinGradle
    ktfmt() // or ktfmt() or prettier()
  }
}

subprojects {
  afterEvaluate {
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
      kotlin {
        target("src/**/*.kt")
        ktfmt()
        endWithNewline()
      }
      kotlinGradle {
        target("*.gradle.kts", "src/**/*.gradle.kts") // default target for kotlinGradle
        ktfmt() // or ktfmt() or prettier()
        endWithNewline()
      }
      java {
        importOrder()
        removeUnusedImports()
        cleanthat()
        palantirJavaFormat().style("GOOGLE")
        target("src/**/*.java", "build/generated/sources/serializable_function/**/*.java")
        endWithNewline()
      }
    }
  }
}

allprojects { repositories { mavenCentral() } }
