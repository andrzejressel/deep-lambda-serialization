include("app")

pluginManagement {
  //  val pluginVersion: String by settings
  //  plugins { id("pl.andrzejressel.deeplambdaserialization").version("DEV-SNAPSHOT") }
  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}
