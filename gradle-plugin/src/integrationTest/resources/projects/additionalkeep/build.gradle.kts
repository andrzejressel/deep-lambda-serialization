import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.authentication.http.HttpHeaderAuthentication

allprojects {
  repositories {
    mavenCentral()
    mavenLocal()
  }

  repositories {
    maven {
      url = uri("https://maven.pkg.github.com/andrzejressel/simple-java-serialization")
      credentials(HttpHeaderCredentials::class) {
        name = "Authorization"
        value = "Bearer ${project.findProperty("gpr.token")}"
      }
      authentication { create<HttpHeaderAuthentication>("header") }
    }
  }
}
