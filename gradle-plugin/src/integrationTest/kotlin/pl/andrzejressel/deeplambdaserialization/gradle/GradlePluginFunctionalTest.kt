package pl.andrzejressel.deeplambdaserialization.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class GradlePluginFunctionalTest {

    @Test
    fun canRunTask() {
        val dir = Paths.get(javaClass.getResource("/integration.pointer")!!.toURI()).parent

        // Run the build
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("build")
            .withProjectDir(dir.resolve("projects/basic").toFile())
            .withDebug(true)
            .build()

        // Verify the result
//        assertThat(result.output)
//            .contains("Hello from plugin 'pl.andrzejressel.deeplambdaserialization'")
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
