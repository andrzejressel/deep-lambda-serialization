import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure

group = "pl.andrzejressel.djcs"

plugins {
    id("com.palantir.git-version") version "3.0.0"
    alias(libs.plugins.kotlin).apply(false)
}

val versionDetails: Closure<VersionDetails> by extra
val details = versionDetails()

version = if(details.isCleanTag) {
    details.lastTag.removePrefix("v")
} else {
    "DEV"
}

allprojects {
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
}
