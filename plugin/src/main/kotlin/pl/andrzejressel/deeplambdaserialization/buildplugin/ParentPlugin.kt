package pl.andrzejressel.deeplambdaserialization.buildplugin

import com.diffplug.gradle.spotless.SpotlessExtension
import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class ParentPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    //    if (target.parent == null) {

    target.configure<SpotlessExtension> {
      kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktfmt() // or ktfmt() or prettier()
      }
    }

    val versionDetails: Closure<VersionDetails> by target.extra
    val details = versionDetails()

    target.version =
        if (details.isCleanTag) {
          val lastTag = details.lastTag
          if (lastTag.startsWith("v")) {
            // Release
            details.lastTag.removePrefix("v")
          } else {
            // main
            "main-SNAPSHOT"
          }
        } else {
          "DEV-SNAPSHOT"
        }
  }
  //  }
}
