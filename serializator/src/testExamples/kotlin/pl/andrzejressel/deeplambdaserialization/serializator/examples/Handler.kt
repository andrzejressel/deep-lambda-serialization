package pl.andrzejressel.deeplambdaserialization.serializator.examples

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import pl.andrzejressel.deeplambdaserialization.lib.JavaClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunctionN
import pl.andrzejressel.deeplambdaserialization.serializator.BuildInfo
import pl.andrzejressel.deeplambdaserialization.serializator.LambdaSerializator

abstract class AbstractLambdaGeneratorTest {

  protected fun save(name: String, f: SerializableFunctionN) {
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
    val javaClzDir: Path =
        BuildInfo.location.resolve("build/classes/java/testExamples").toAbsolutePath()
    val kotlinClzDir: Path =
        BuildInfo.location.resolve("build/classes/kotlin/testExamples").toAbsolutePath()
    val dependencies: String = BuildInfo.dependencies
    val supportLib: String = BuildInfo.supportLib

    val classPath =
        dependencies
            .split(",")
            .asSequence()
            .map { Paths.get(it) }
            .distinct()
            .filterNot { it.toString().contains("groovy") }
            .filterNot { it.toString().contains("gradle-worker.jar") }
            .filterNot { it.last().toString().contains("gradle") }
            .filterNot { it.toString().contains("bytebuddy") }
            .filterNot { it.toString().contains("log4j") }
            .filterNot { it.toString().contains("wrapper") }
            .toSet()

    val supportLibList =
        supportLib.split(",").filter { it.isNotEmpty() }.map { Paths.get(it) }.toSet()

    val applicationClassPath = setOf(javaClzDir, kotlinClzDir)

    return LambdaSerializator(
        classPath,
        supportLibList,
        applicationClassPath,
        Paths.get("build", "examples", "jars"),
        listOf())
  }
}
