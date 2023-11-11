// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.gradle

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import pl.andrzejressel.deeplambdaserialization.serializator.LambdaSerializator

@CacheableTask
abstract class GenerateLambdaJars : DefaultTask() {
  @get:InputFiles @get:Classpath abstract val allClasses: ListProperty<File>
  @get:InputFiles @get:Classpath abstract val dependencies: ListProperty<File>
  @get:InputFiles @get:Classpath abstract val classes: ListProperty<File>
  @get:Input abstract val additionalProguardOptions: ListProperty<String>
  @get:OutputDirectory abstract val output: DirectoryProperty

  @TaskAction
  fun generate() {
    val other =
        allClasses.get().map { it.toPath() }.toSet() -
            dependencies.get().map { it.toPath() }.toSet()

    logger.debug("All classes:\n${allClasses.get().joinToString(separator = "\n")}")
    logger.debug("Dependencies:\n${dependencies.get().joinToString(separator = "\n")}")
    logger.debug("Classes:\n${classes.get().joinToString(separator = "\n")}")
    logger.debug("Other:\n${other.joinToString(separator = "\n")}")

    val sl =
        LambdaSerializator(
            dependencies = other,
            supportLib = dependencies.get().map { it.toPath() }.toSet(),
            classes = classes.get().map { it.toPath() }.toSet(),
            output = output.get().asFile.toPath().resolve("META-INF"),
            tmpDirectory = temporaryDir.toPath(),
            additionalProguardOptions = additionalProguardOptions.get(),
        )

    logger.debug("Classes to generate: [{}]", sl.getClasses())

    sl.getClasses().forEach { sl.createJar(it) }
  }
}
