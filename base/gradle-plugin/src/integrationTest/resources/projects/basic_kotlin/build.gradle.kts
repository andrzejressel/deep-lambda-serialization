plugins {
  kotlin("jvm") version "1.9.10"
  application
  id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
  mavenCentral()
  mavenLocal()
}

application { mainClass.set("com.example.project.MainKt") }
