import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure

group = "pl.andrzejressel.deeplambdaserialization"

plugins {
  id("com.palantir.git-version") version "3.0.0"
  alias(libs.plugins.kotlin).apply(false)
  alias(libs.plugins.spotless)
  id("com.vanniktech.maven.publish") version "0.25.3" apply false
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
      }
      kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        target("src/**/*.gradle.kts")
        ktfmt() // or ktfmt() or prettier()
      }
      java {
        importOrder()
        removeUnusedImports()
        cleanthat()
        googleJavaFormat()
        target("src/**/*.java")
      }
    }
  }
}

allprojects { repositories { mavenCentral() } }
