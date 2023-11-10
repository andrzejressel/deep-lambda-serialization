import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
  `kotlin-dsl`
  alias(libs.plugins.kotlin)
  `java-gradle-plugin`
  alias(libs.plugins.spotless)
}

gradlePlugin {
  plugins {
    create("parentPlugin") {
      id = "pl.andrzejressel.deeplambdaserialization.buildplugin.parent"
      implementationClass = "pl.andrzejressel.deeplambdaserialization.buildplugin.ParentPlugin"
    }
    create("childPlugin") {
      id = "pl.andrzejressel.deeplambdaserialization.buildplugin.child"
      implementationClass = "pl.andrzejressel.deeplambdaserialization.buildplugin.ChildPlugin"
    }
  }
}

dependencies {
  compileOnly(libs.spotless.gradle.plugin)
  compileOnly(libs.git.version.gradle.plugin)
  compileOnly(libs.maven.publish.gradle.plugin)
}

repositories {
  mavenCentral()
  maven { url = uri("https://plugins.gradle.org/m2/") }
}

configure<SpotlessExtension> {
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
    target("src/**/*.java")
    endWithNewline()
  }
}
