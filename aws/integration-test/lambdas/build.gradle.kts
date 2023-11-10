import pl.andrzejressel.deeplambdaserialization.aws.gradle.DeepSerializationAWSPlugin
import pl.andrzejressel.deeplambdaserialization.build.ChildPlugin.Companion.License
import pl.andrzejressel.deeplambdaserialization.build.ChildPlugin.Companion.childSetup
import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPlugin

plugins {
  kotlin("jvm")
  alias(libs.plugins.spotless)
  kotlin("plugin.serialization") version "1.9.10"
  alias(libs.plugins.maven.publish)
}

childSetup(License.GPL)

buildscript {
  dependencies { classpath("pl.andrzejressel.deeplambdaserialization:gradle-plugin") }
  dependencies { classpath("pl.andrzejressel.deeplambdaserialization.aws:gradle-plugin") }
}

apply<DeepSerializationPlugin>()

apply<DeepSerializationAWSPlugin>()

dependencies {
  implementation("pl.andrzejressel.deeplambdaserialization:lib-kotlin:$version")
  implementation(libs.kotlinx.serialization.json)
}
