import java.nio.file.Files
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

plugins {
    `java-library`
    `jvm-test-suite`
    `maven-publish`
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter)
    implementation(libs.jetbrains.annotations)
    implementation("pl.andrzejressel.sjs:serializator:0.0.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(PASSED, SKIPPED, FAILED)
    }
}

val generateBuildInfo by tasks.registering {
    inputs.property("version", parent!!.version)
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
            val serializatorFields = alphabet.take(i).joinToString(separator = "\n") { c ->
                "                    protected Serializator<$c> ${c.lowercaseChar()};"
            }

            Files.createDirectories(dir)

            val clz = """
                package pl.andrzejressel.deeplambdaserialization.lib;
                
                import pl.andrzejressel.sjs.serializator.Serializator;
                
                public abstract class SerializableFunction${i}<$genericClasses> extends SerializableFunctionN {
                    public abstract RET execute(${arguments});
$serializatorFields
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

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = mvnGroupId
            artifactId = mvnArtifactId
            version = mvnVersion
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/andrzejressel/deep-java-code-serialization")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
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

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = false
    }
}

tasks.named("check") {
    dependsOn("jacocoTestReport")
}