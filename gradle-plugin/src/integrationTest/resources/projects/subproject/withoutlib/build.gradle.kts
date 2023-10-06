import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPluginExtension

plugins {
    application
    id("pl.andrzejressel.deeplambdaserialization") version "DEV"
}

dependencies {
    implementation(project(":lib"))
}

configure<DeepSerializationPluginExtension> {
    addProject(project(":lib"))
}

application {
    mainClass.set("com.test.withoutlib.Main")
}
