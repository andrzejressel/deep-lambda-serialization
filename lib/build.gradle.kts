import com.palantir.javaformat.java.Formatter
import com.palantir.javaformat.java.JavaFormatterOptions
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  `java-library`
  `jvm-test-suite`
  jacoco
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

buildscript { dependencies { classpath("com.palantir.javaformat:palantir-java-format:2.38.0") } }

repositories { mavenCentral() }

dependencies {
  testImplementation(platform(libs.junit.bom))
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.junit.jupiter)
  implementation(libs.jetbrains.annotations)
  api(libs.dto.serializator)
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
          val contextGenericClasses =
              (alphabet.take(i) + "CONTEXT" + "RET").joinToString(separator = ", ")
          val arguments =
              alphabet.take(i).joinToString(separator = ", ") { c -> "$c ${c.lowercaseChar()}" }
          val contextArguments =
              (listOf("CONTEXT context") + alphabet.take(i).map { c -> "$c ${c.lowercaseChar()}" })
                  .joinToString(", ")
          val arguments2 =
              alphabet
                  .take(i)
                  .mapIndexed { index, c -> "($c) args[$index]" }
                  .joinToString(separator = ", ")
          val contextArguments2 =
              (listOf("context") + alphabet.take(i).mapIndexed { index, c -> "($c) args[$index]" })
                  .joinToString(separator = ", ")
          val serializatorFields =
              alphabet.take(i).joinToString(separator = "\n") { c ->
                "                    protected abstract Serializator<$c> getArg${c - 'A' + 1}Serializator();"
              }

          val getArgumentSerializatorContent =
              alphabet
                  .take(i)
                  .map { c -> "                    l.add(getArg${c - 'A' + 1}Serializator());" }
                  .joinToString(separator = "\n")
          val getArgumentsSerializator =
              """
                                @Override
                                final public List<Serializator<?>> getInputSerializators() {
                                    var l = new ArrayList<Serializator<?>>();
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
                                
                                public abstract class SerializableFunction$i<$genericClasses> extends SerializableFunctionN<RET> {
                                    public abstract RET execute($arguments);
                                    @Override
                                    public final RET execute(Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($arguments2);
                                    }
                                }
                """
                  .trimIndent()

          val serializableFunctionWithContext =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                public abstract class SerializableFunctionWithContext$i<$contextGenericClasses> extends SerializableFunctionWithContextN<RET, CONTEXT> {
                                    public abstract RET execute($contextArguments);
                                    @Override
                                    public final RET execute(CONTEXT context, Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($contextArguments2);
                                    }
                                }
                """
                  .trimIndent()

          val inputClz =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import java.util.ArrayList;
                                import java.util.List;
                                import pl.andrzejressel.dto.serializator.Serializator;

                                public abstract class SerializableInputFunction$i<$genericClasses> extends SerializableInputFunctionN<RET> {
                                    public abstract RET execute($arguments);
                $serializatorFields
                $getArgumentsSerializator
                                    @Override
                                    public final RET execute(Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($arguments2);
                                    }
                                }
                """
                  .trimIndent()

          val serializableInputFunctionWithContext =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import java.util.ArrayList;
                                import java.util.List;
                                import pl.andrzejressel.dto.serializator.Serializator;

                                public abstract class SerializableInputFunctionWithContext$i<$contextGenericClasses> extends SerializableInputFunctionWithContextN<RET, CONTEXT> {
                                    public abstract RET execute($contextArguments);
                $serializatorFields
                $getArgumentsSerializator
                                    @Override
                                    public final RET execute(CONTEXT context, Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($contextArguments2);
                                    }
                                }
                """
                  .trimIndent()

          val inputOutputClz =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import java.util.ArrayList;
                                import java.util.List;
                                import pl.andrzejressel.dto.serializator.Serializator;

                                public abstract class SerializableInputOutputFunction$i<$genericClasses> extends SerializableInputOutputFunctionN<RET> {
                                    public abstract RET execute($arguments);
                $serializatorFields
                $getArgumentsSerializator
                                    @Override
                                    public final RET execute(Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($arguments2);
                                    }
                                }
                """
                  .trimIndent()

          val inputOutputFunctionWithContext =
              """
                                package pl.andrzejressel.deeplambdaserialization.lib;
                                
                                import java.util.ArrayList;
                                import java.util.List;
                                import pl.andrzejressel.dto.serializator.Serializator;

                                public abstract class SerializableInputOutputFunctionWithContext$i<$contextGenericClasses> extends SerializableInputOutputFunctionWithContextN<RET, CONTEXT> {
                                    public abstract RET execute($contextArguments);
                $serializatorFields
                $getArgumentsSerializator
                                    @Override
                                    public final RET execute(CONTEXT context, Object[] args) {
                                        if (args.length != $i) {
                                            throw new IllegalArgumentException(String.format("Array must have $i ${if (i == 1) "element" else "elements"}, it has %d instead", args.length));
                                        }
                                        ${if (i != 0) "//noinspection unchecked" else ""}
                                        return execute($contextArguments2);
                                    }
                                }
                """
                  .trimIndent()

          val formatter =
              Formatter.createFormatter(
                  JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.GOOGLE).build())

          Files.writeString(
              classDir.resolve("SerializableFunction$i.java"),
              formatter.formatSource(serializableFunction))
          Files.writeString(
              classDir.resolve("SerializableFunctionWithContext$i.java"),
              formatter.formatSource(serializableFunctionWithContext))
          Files.writeString(
              classDir.resolve("SerializableInputFunction$i.java"),
              formatter.formatSource(inputClz))
          Files.writeString(
              classDir.resolve("SerializableInputFunctionWithContext$i.java"),
              formatter.formatSource(serializableInputFunctionWithContext))
          Files.writeString(
              classDir.resolve("SerializableInputOutputFunction$i.java"),
              formatter.formatSource(inputOutputClz))
          Files.writeString(
              classDir.resolve("SerializableInputOutputFunctionWithContext$i.java"),
              formatter.formatSource(inputOutputFunctionWithContext))
        }
      }
    }

sourceSets { main { java { srcDirs(generateSerializableFunction, generateBuildInfo) } } }

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

mavenPublishing {
  coordinates(mvnGroupId, mvnArtifactId, mvnVersion)

  pom {
    licenses {
      license {
        name = "Gnu Lesser General Public License"
        url = "http://www.gnu.org/licenses/lgpl.txt"
        distribution = "http://www.gnu.org/licenses/lgpl.txt"
      }
    }
  }
}

tasks.jacocoTestReport {
  reports {
    xml.required = true
    html.required = false
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }

afterEvaluate { tasks.named("spotlessJava") { dependsOn("generateSerializableFunction") } }
