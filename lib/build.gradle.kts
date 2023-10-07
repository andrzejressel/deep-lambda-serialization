import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  `java-library`
  `jvm-test-suite`
  `maven-publish`
  jacoco
  alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

dependencies {
  testImplementation(platform(libs.junit.bom))
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.junit.jupiter)
  implementation(libs.jetbrains.annotations)
  api(libs.sjs)
}

tasks.test {
  useJUnitPlatform()
  testLogging { events(PASSED, SKIPPED, FAILED) }
}

val generateBuildInfo by
    tasks.registering {
      inputs.property("version", parent!!.version)
      outputs.dir(layout.buildDirectory.dir("generated/sources/build_info"))
      doLast {
        val dir = outputs.files.single().toPath()

        val clz =
            """
            package pl.andrzejressel.deeplambdaserialization.lib;
            
            public class BuildInfo {
                public static String version = "${this.inputs.properties["version"]}";
                public static String jarTag = "deep-lambda-serialization-lib";
            }
            """
                .trimIndent()

        dir.resolve("pl/andrzejressel/deeplambdaserialization/lib")
            .createDirectories()
            .resolve("BuildInfo.java")
            .writeText(clz)
      }
    }

val generateSerializableFunction by
    tasks.registering {
      outputs.dir(layout.buildDirectory.dir("generated/sources/serializable_function"))
      doLast {
        val dir = outputs.files.single().toPath()
        val classDir =
            dir.resolve("pl/andrzejressel/deeplambdaserialization/lib").createDirectories()
        val alphabet = 'A'..'Z'

        (0..32).forEach { i ->
          val genericClasses = (alphabet.take(i) + "RET").joinToString(separator = ", ")
          val arguments =
              alphabet.take(i).joinToString(separator = ", ") { c -> "$c ${c.lowercaseChar()}" }
          val arguments2 =
              alphabet
                  .take(i)
                  .mapIndexed { index, c -> "($c) args[$index]" }
                  .joinToString(separator = ", ")
          val serializatorFields =
              alphabet.take(i).joinToString(separator = "\n") { c ->
                "                    protected abstract Serializator<$c> get${c}Serializator();"
              }

          val getArgumentSerializatorContent =
              alphabet
                  .take(i)
                  .map { c ->
                    "                    l.add((Serializator<Object>) get${c}Serializator());"
                  }
                  .joinToString(separator = "\n")
          val getArgumentsSerializator =
              """
                                @Override
                                final public List<Serializator<Object>> getInputSerializators() {
                                    var l = new ArrayList<Serializator<Object>>();
                $getArgumentSerializatorContent
                                    return l;
                                }
                """
                  .trimIndent()
                  .lines()
                  .joinToString(separator = "\n") { "                    $it" }

          Files.createDirectories(dir)

          val serializableFunction =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import pl.andrzejressel.sjs.serializator.Serializator;
                                import java.util.ArrayList;
                                import java.util.List;
                                
                                public abstract class SerializableFunction$i<$genericClasses> extends SerializableFunctionN {
                                    public abstract RET execute($arguments);
                                    @Override
                                    public final Object execute(Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($arguments2);
                                    }
                                }
                """
                  .trimIndent()

          val inputClz =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import pl.andrzejressel.sjs.serializator.Serializator;
                                import java.util.ArrayList;
                                import java.util.List;

                                public abstract class SerializableInputFunction$i<$genericClasses> extends SerializableInputFunctionN {
                                    public abstract RET execute($arguments);
                $serializatorFields
                $getArgumentsSerializator
                                    @Override
                                    public final Object execute(Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($arguments2);
                                    }
                                }
                """
                  .trimIndent()

          val inputOutputClz =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import pl.andrzejressel.sjs.serializator.Serializator;
                                import java.util.ArrayList;
                                import java.util.List;

                                public abstract class SerializableInputOutputFunction$i<$genericClasses> extends SerializableInputOutputFunctionN {
                                    public abstract RET execute($arguments);
                                    public abstract Serializator<RET> getReturnSerializator();
                $serializatorFields
                $getArgumentsSerializator
                                    @Override
                                    public final Object execute(Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($arguments2);
                                    }
                                    @Override
                                    public final Serializator<Object> getOutputSerializator() {
                                        return (Serializator<Object>) getReturnSerializator();
                                    }
                                }
                """
                  .trimIndent()

          Files.writeString(classDir.resolve("SerializableFunction$i.java"), serializableFunction)
          Files.writeString(classDir.resolve("SerializableInputFunction$i.java"), inputClz)
          Files.writeString(
              classDir.resolve("SerializableInputOutputFunction$i.java"), inputOutputClz)
        }
      }
    }

sourceSets { main { java { srcDirs(generateSerializableFunction, generateBuildInfo) } } }

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
        "deep-lambda-serialization-lib" to "true",
    )
  }
}

tasks.jacocoTestReport {
  reports {
    xml.required = true
    html.required = false
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }
