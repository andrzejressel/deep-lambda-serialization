import pl.andrzejressel.deeplambdaserialization.build.ChildPlugin.Companion.License
import pl.andrzejressel.deeplambdaserialization.build.ChildPlugin.Companion.childSetup

plugins {
  kotlin("jvm")
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.spotless)
}

childSetup(License.LGPL)

dependencies {
  implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
  compileOnly("pl.andrzejressel.deeplambdaserialization:entrypoint:$version")
}
