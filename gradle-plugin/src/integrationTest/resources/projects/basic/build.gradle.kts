plugins {
  java
  application
  id("pl.andrzejressel.deeplambdaserialization")
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies { implementation("pl.andrzejressel.deeplambdaserialization:lib:DEV-SNAPSHOT") }

application { mainClass.set("com.example.project.Main") }
