// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.serializator.examples

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import pl.andrzejressel.deeplambdaserialization.lib.JavaClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction
import pl.andrzejressel.deeplambdaserialization.serializator.BuildInfo
import pl.andrzejressel.deeplambdaserialization.serializator.LambdaSerializator

abstract class AbstractLambdaGeneratorTest {

  protected fun save(name: String, f: SerializableFunction) {
    val projectPath: Path = BuildInfo.location

    synchronized(this) {
      projectPath
          .resolve("build/examples/")
          .createDirectories()
          .resolve("${name}.txt")
          .writeText(NameUtils.getJarName(f))
    }

    val lambdaSerializator = createLambdaSerializator()
    lambdaSerializator.createJar(JavaClassName(f))
  }

  private fun createLambdaSerializator(): LambdaSerializator {
    val javaClzDir = BuildInfo.location.resolve("build/classes/java/testExamples").toAbsolutePath()
    val kotlinClzDir =
        BuildInfo.location.resolve("build/classes/kotlin/testExamples").toAbsolutePath()
    val classPath =
        BuildInfo.dependencies.split(",").filter { it.isNotEmpty() }.map { Paths.get(it) }.toSet()
    val supportLib =
        BuildInfo.supportLib.split(",").filter { it.isNotEmpty() }.map { Paths.get(it) }.toSet()

    val applicationClassPath = setOf(javaClzDir, kotlinClzDir)

    return LambdaSerializator(
        classPath,
        supportLib,
        applicationClassPath,
        Paths.get("build", "examples", "jars"),
        Paths.get("build", "tmp", "examples", "jars"),
        listOf())
  }
}
