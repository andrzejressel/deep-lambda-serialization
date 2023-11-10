plugins { id("com.gradle.enterprise") version ("3.15.1") }

if (!System.getenv("CI").isNullOrEmpty()) {
  gradleEnterprise {
    buildScan {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
    }
  }
}

rootProject.name = "deep-lambda-serialization"

include(":gradle-plugin", ":lib", ":serializator", ":entrypoint", ":lib-kotlin")
