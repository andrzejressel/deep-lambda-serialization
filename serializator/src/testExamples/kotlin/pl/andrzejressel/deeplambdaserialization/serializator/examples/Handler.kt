package pl.andrzejressel.deeplambdaserialization.serializator.examples

import pl.andrzejressel.deeplambdaserialization.serializator.BuildInfo
import pl.andrzejressel.deeplambdaserialization.serializator.LambdaSerializator
import pl.andrzejressel.deeplambdaserialization.lib.JavaClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunctionN
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

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
        val javaClzDir: Path = BuildInfo.location.resolve("build/classes/java/testExamples").toAbsolutePath()
        val kotlinClzDir: Path = BuildInfo.location.resolve("build/classes/kotlin/testExamples").toAbsolutePath()
        val dependencies: String = BuildInfo.dependencies

        val classPath = dependencies
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
            .toList()

//        val classPath = System.getProperty("java.class.path").split(';')
//            .asSequence()
//            .map { Paths.get(it) }
//            .distinct()
//            .filterNot { it.toString().contains("groovy") }
//            .filterNot { it.toString().contains("gradle-worker.jar") }
//            .filterNot { it.last().toString().contains("gradle") }
//            .filterNot { it.toString().contains("bytebuddy") }
//            .filterNot { it.toString().contains("log4j") }
//            .filterNot { it.toString().contains("wrapper") }
//            .toList()

//        Assertions.assertThat(classPath).contains(javaClzDir, kotlinClzDir)

        val applicationClassPath = setOf(javaClzDir, kotlinClzDir)

        return LambdaSerializator(
            classPath.toList(),
            applicationClassPath.toList(),
            Paths.get("build", "examples", "jars")
        )
    }

}