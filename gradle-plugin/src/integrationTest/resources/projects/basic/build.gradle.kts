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

dependencies {
    implementation("pl.andrzejressel.deeplambdaserialization:lib:0.0.1")
}

configure<DeepSerializationPluginExtension> {
    classes.set(configurations.runtimeClasspath)
}

application {
    mainClass.set("com.example.project.Main")
}