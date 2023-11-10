plugins {
  application
  id("pl.andrzejressel.deeplambdaserialization") version "DEV-SNAPSHOT"
}

application { mainClass.set("com.test.withlib.Main") }

dependencies { implementation(project(":lib")) }
