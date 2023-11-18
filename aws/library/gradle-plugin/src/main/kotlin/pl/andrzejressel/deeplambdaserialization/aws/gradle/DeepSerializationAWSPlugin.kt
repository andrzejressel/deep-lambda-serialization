// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.aws.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import pl.andrzejressel.deeplambdaserialization.aws.lib.BuildInfo
import pl.andrzejressel.deeplambdaserialization.gradle.DeepSerializationPluginExtension

@Suppress("unused")
class DeepSerializationAWSPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    if (!project.pluginManager.hasPlugin("pl.andrzejressel.deeplambdaserialization")) {
      project.logger.error(
          "Required plugin [pl.andrzejressel.deeplambdaserialization] is not applied")
      return
    }

    project.dependencies {
      add(
          "implementation",
          "pl.andrzejressel.deeplambdaserialization.aws:handler:${BuildInfo.version}")
    }

    project.plugins.withId("pl.andrzejressel.deeplambdaserialization") {
      project.configure<DeepSerializationPluginExtension> {
        addKeep("public interface com.amazonaws.services.lambda.runtime.** {*;}")
        addKeep("public class pl.andrzejressel.deeplambdaserialization.aws.handler.Handler {*;}")
      }
    }
  }
}
