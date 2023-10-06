import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure

group = "pl.andrzejressel.djcs"

plugins {
  id("com.palantir.git-version") version "3.0.0"
  alias(libs.plugins.kotlin).apply(false)
  alias(libs.plugins.spotless)
}

val versionDetails: Closure<VersionDetails> by extra
val details = versionDetails()

version =
    if (details.isCleanTag) {
      details.lastTag.removePrefix("v")
    } else {
      "DEV"
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
      kotlin { ktfmt() }
      kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktfmt() // or ktfmt() or prettier()
      }
      java {
        importOrder()
        removeUnusedImports()
        cleanthat()
        googleJavaFormat()
        targetExclude("build/generated/**/*.java")
      }
    }
  }
}

allprojects {
  repositories {
    mavenCentral()
    maven {
      url = uri("https://maven.pkg.github.com/andrzejressel/simple-java-serialization")
      credentials(HttpHeaderCredentials::class) {
        name = "Authorization"
        value = "Bearer ${project.findProperty("gpr.token") as String}"
      }
      authentication { create<HttpHeaderAuthentication>("header") }
    }
  }
}
