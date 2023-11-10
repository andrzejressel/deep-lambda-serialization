pluginManagement {
  repositories {
    mavenCentral()
    //    mavenLocal()
    gradlePluginPortal()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
  }
}

plugins { id("com.gradle.enterprise") version ("3.15.1") }

if (!System.getenv("CI").isNullOrEmpty()) {
  gradleEnterprise {
    buildScan {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
    }
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    //    mavenLocal()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
  }
  versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}

rootProject.name = "deep-lambda-serialization"

include(":gradle-plugin", ":lib", ":serializator", ":entrypoint", ":lib-kotlin")
