import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.authentication.http.HttpHeaderAuthentication
import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPluginExtension

plugins {
    java
    application
    id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
    mavenCentral()
    mavenLocal()
}

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/andrzejressel/simple-java-serialization")
        credentials(HttpHeaderCredentials::class) {
            name = "Authorization"
            value = "Bearer ${project.findProperty("gpr.token") as String}"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

dependencies {
    implementation("pl.andrzejressel.djcs:lib:DEV")
}

configure<DeepSerializationPluginExtension> {
    classes.set(configurations.runtimeClasspath)
}

application {
    mainClass.set("com.example.project.Main")
}