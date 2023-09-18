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
