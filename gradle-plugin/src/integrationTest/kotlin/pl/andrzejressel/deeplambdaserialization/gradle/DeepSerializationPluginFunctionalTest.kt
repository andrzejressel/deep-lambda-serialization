package pl.andrzejressel.deeplambdaserialization.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.zeroturnaround.zip.ZipUtil
import pl.andrzejressel.deeplambdaserialization.gradle.integrationtest.BuildInfo
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import kotlin.io.path.name
import kotlin.io.path.toPath

class DeepSerializationPluginFunctionalTest {

    private val jarPattern = Pattern.compile("JAR: \\[(.*)]")!!

    @Test
    fun canRunBasicJavaProject() {
        val expectedClasses = mapOf(
            "com.example.project.Main\$1.jar" to setOf(
                "META-INF/MANIFEST.MF",
                "EntryPoint.class",
                "com/example/project/Main.class",
                "com/example/project/Main$1.class",
                "pl/andrzejressel/deeplambdaserialization/lib/Runner.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunction0.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunctionN.class",
                "pl/andrzejressel/sjs/serializator/Serializator.class",
                "pl/andrzejressel/sjs/serializator/SingleSerializator.class",
                "pl/andrzejressel/sjs/serializator/StringSerializator.class"
            )
        )
        val projectName = "basic"

        runTest(projectName, expectedClasses)
    }

    @Test
    fun canRunBasicKotlinProject() {
        val expectedClasses = mapOf(
            "com.example.project.MainKt\$main\$lambda\$1.jar" to setOf(
                "META-INF/MANIFEST.MF",
                "EntryPoint.class",
                "com/example/project/MainKt.class",
                "com/example/project/MainKt\$main\$lambda$1.class",
                "kotlin/jvm/internal/Intrinsics.class",
                "pl/andrzejressel/deeplambdaserialization/lib/Runner.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunction0.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunctionN.class",
                "pl/andrzejressel/sjs/serializator/Serializator.class",
                "pl/andrzejressel/sjs/serializator/SingleSerializator.class",
                "pl/andrzejressel/sjs/serializator/StringSerializator.class"
            )
        )
        val projectName = "basic_kotlin"

        runTest(projectName, expectedClasses)
    }

    @Test
    fun checkAddProjectToDependencies() {
        val expectedClasses = mapOf(
            "com.test.withlib.Main$1.jar" to setOf(
                "META-INF/MANIFEST.MF",
                "EntryPoint.class",
                "com/test/lib/SharedLib.class",
                "com/test/withlib/Main.class",
                "com/test/withlib/Main$1.class",
                "pl/andrzejressel/deeplambdaserialization/lib/Runner.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunction0.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunctionN.class",
                "pl/andrzejressel/sjs/serializator/Serializator.class",
                "pl/andrzejressel/sjs/serializator/SingleSerializator.class",
                "pl/andrzejressel/sjs/serializator/StringSerializator.class"
            ),
            "com.test.withoutlib.Main$1.jar" to setOf(
                "META-INF/MANIFEST.MF",
                "EntryPoint.class",
                "com/test/withoutlib/Main.class",
                "com/test/withoutlib/Main$1.class",
                "pl/andrzejressel/deeplambdaserialization/lib/Runner.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunction0.class",
                "pl/andrzejressel/deeplambdaserialization/lib/SerializableFunctionN.class",
                "pl/andrzejressel/sjs/serializator/Serializator.class",
                "pl/andrzejressel/sjs/serializator/SingleSerializator.class",
                "pl/andrzejressel/sjs/serializator/StringSerializator.class"
            ),
        )
        val projectName = "subproject"

        runTest(projectName, expectedClasses)
    }

    private fun runTest(projectName: String, expectedClasses: Map<String, Set<String>>) {
        val dir = Paths.get(javaClass.getResource("/integration.pointer")!!.toURI()).parent
        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments(
                "-Pgpr.token=${BuildInfo.token}",
                "clean",
                "run",
                "--stacktrace"
            )
            .withProjectDir(dir.resolve("projects/$projectName").toFile())
            .withDebug(true)
            .build()

        // Verify the result
        assertThat(result.output)
            .containsPattern(jarPattern)

        val jarUrlStrings = result.output
            .lines()
            .mapNotNull { line ->
                val m = jarPattern.matcher(line)
                if (m.matches()) {
                    m.group(1)
                } else {
                    null
                }
            }

        val map = jarUrlStrings.map { jarUrl ->
            val jar = URL(jarUrl).toURI().toPath()
            val zipEntries = getZipEntries(jar)
            jar.name to zipEntries
        }.toMap()

        assertThat(map)
            .containsExactlyInAnyOrderEntriesOf(expectedClasses)
    }

    private fun getZipEntries(zip: Path): Set<String> {
        val l = mutableSetOf<String>()
        ZipUtil.iterate(zip.toFile()) { entry ->
            if (!entry.isDirectory) {
                l.add(entry.name)
            }
        }
        return l
    }

    @Test
    fun withoutJava() {
        val dir = Paths.get(javaClass.getResource("/integration.pointer")!!.toURI()).parent

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("deeplambdaserialization")
            .withProjectDir(dir.resolve("projects/without_java").toFile())
            .withDebug(true)
            .buildAndFail()

        // Verify the result
        assertThat(result.output)
            .contains("Cannot find 'implementation' configuration. Probably java plugin is not applied")
    }

}
