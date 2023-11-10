import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPluginExtension

plugins {
  application
  id("pl.andrzejressel.deeplambdaserialization")
  id("pl.andrzejressel.deeplambdaserialization.aws")
}

application { mainClass.set("com.test.additionalkeep.Main") }

configure<DeepSerializationPluginExtension> {
  addKeep("public class com.test.additionalkeep.AdditionalClass {*;}")
}
