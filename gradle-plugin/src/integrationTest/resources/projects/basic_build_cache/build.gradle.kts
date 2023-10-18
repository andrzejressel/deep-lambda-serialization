plugins {
  java
  id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies { implementation("pl.andrzejressel.deeplambdaserialization:lib:DEV-SNAPSHOT") }
