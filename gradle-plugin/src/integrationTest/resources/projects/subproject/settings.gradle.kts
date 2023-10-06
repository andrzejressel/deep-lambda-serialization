pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
    maven {
      url = uri("https://maven.pkg.github.com/andrzejressel/simple-java-serialization")
      credentials(HttpHeaderCredentials::class) {
        name = "Authorization"
        value = "Bearer ${extra["gpr.token"]}"
      }
      authentication { create<HttpHeaderAuthentication>("header") }
    }
  }
}

include("lib", "withlib", "withoutlib")
