import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPluginExtension

plugins {
    java
    id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
    mavenLocal()
}

configure<DeepSerializationPluginExtension> {
    dependencies.set(configurations.runtimeClasspath)
    output.set(layout.buildDirectory.dir("generated/sources/build_info"))
}