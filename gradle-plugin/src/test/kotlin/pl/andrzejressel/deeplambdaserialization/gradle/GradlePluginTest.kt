package pl.andrzejressel.deeplambdaserialization.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class GradlePluginTest {
    @Test
    fun pluginRegistersATask() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java")
        project.plugins.apply("pl.andrzejressel.deeplambdaserialization")

        // Verify the result
        assertNotNull(project.tasks.findByName("deeplambdaserialization"))
    }
}
