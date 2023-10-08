plugins {
  kotlin("jvm") version "1.9.10"
  application
  id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies { implementation("pl.andrzejressel.djcs:lib:DEV") }

application { mainClass.set("com.example.project.MainKt") }
