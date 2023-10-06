import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPluginExtension

plugins {
  application
  id("pl.andrzejressel.deeplambdaserialization") version "DEV"
}

application { mainClass.set("com.test.additionalkeep.Main") }

configure<DeepSerializationPluginExtension> {
  addKeep("public class com.test.additionalkeep.AdditionalClass {*;}")
}
