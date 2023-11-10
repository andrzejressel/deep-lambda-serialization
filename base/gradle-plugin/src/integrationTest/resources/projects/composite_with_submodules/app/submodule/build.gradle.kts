plugins {
  application
  id("pl.andrzejressel.deeplambdaserialization")
}

application { mainClass.set("com.test.composite.Main") }

dependencies { implementation("aressel:lib") }

repositories {
  mavenCentral()
  mavenLocal()
}
