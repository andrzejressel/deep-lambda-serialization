import java.nio.file.Files
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

plugins {
    `java-library`
    `jvm-test-suite`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(PASSED, SKIPPED, FAILED)
    }
}

val projectVersion: Provider<String> = providers.gradleProperty("version")

val generateBuildInfo by tasks.registering {
//    inputs.property("version", version)
    outputs.dir(layout.buildDirectory.dir("generated/sources/build_info"))
    doLast {
        val dir = outputs.files.single().toPath()

        val clz = """
                package pl.andrzejressel.deeplambdaserialization.lib;
                
                public class BuildInfo {
                    public static String version = "${this.inputs.properties["version"]}";
                    public static String jarTag = "deep-lambda-serialization-lib";
                }
            """.trimIndent()

        dir
            .resolve("pl/andrzejressel/deeplambdaserialization/lib")
            .createDirectories()
            .resolve("BuildInfo.java")
            .writeText(clz)
    }
}

val generateSerializableFunction by tasks.registering {
    outputs.dir(layout.buildDirectory.dir("generated/sources/serializable_function"))
    doLast {

        val dir = outputs.files.single().toPath()
        val classDir = dir.resolve("pl/andrzejressel/deeplambdaserialization/lib")
            .createDirectories()
        val alphabet = 'A'..'Z'

        (0..32).forEach { i ->
            val genericClasses = (alphabet.take(i) + "RET").joinToString(separator = ", ")
            val arguments = alphabet.take(i).joinToString(separator = ", ") { c -> "$c ${c.lowercaseChar()}" }
            val arguments2 = alphabet.take(i).mapIndexed { index, c ->
                "(${c}) args[${index}]"
            }.joinToString(separator = ", ")

            Files.createDirectories(dir)

            val clz = """
                package pl.andrzejressel.deeplambdaserialization.lib;
                
                public abstract class SerializableFunction${i}<$genericClasses> extends SerializableFunctionN {
                    public abstract RET execute(${arguments});
                    @Override
                    public final Object execute(Object[] args) {
                        if (args.length != ${i}) {
                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                        }
                        ${if (i != 0) "//noinspection unchecked" else ""}
                        return execute(${arguments2});
                    }
                }
            """.trimIndent()

            Files.writeString(classDir.resolve("SerializableFunction${i}.java"), clz)
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs(generateSerializableFunction, generateBuildInfo)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = property("group")!!.toString()
            artifactId = "lib"
            version = projectVersion.get()

            from(components["java"])
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "deep-lambda-serialization-lib" to "true"
        )
    }
}
