plugins {
  java
  application
  id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
  mavenCentral()
  mavenLocal()
}

application { mainClass.set("com.example.project.Main") }
