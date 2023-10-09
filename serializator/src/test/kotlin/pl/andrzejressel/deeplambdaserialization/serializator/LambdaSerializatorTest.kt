package pl.andrzejressel.deeplambdaserialization.serializator

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader
import kotlin.io.path.readText
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.andrzejressel.deeplambdaserialization.lib.JavaClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils

class LambdaSerializatorTest {

  @Test
  fun shouldMinimizeKotlinLambda() {
    val examples = BuildInfo.location.resolve("build/examples")
    val className = JavaClassName(examples.resolve("kotlin_basic.txt").readText())
    val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

    val cl =
        URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

    val clz = cl.loadClass("pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint")

    val constructorMethodType = MethodType.methodType(Any::class.java, Array<Any>::class.java)
    val constructorMethodHandle =
        MethodHandles.publicLookup().findStatic(clz, "execute", constructorMethodType)

    val inst = constructorMethodHandle.invoke(arrayOf(1, 2))

    assertThat(inst).isEqualTo("3")
  }

  @Test
  fun shouldMinimizeJavaLambda() {
    val examples = BuildInfo.location.resolve("build/examples")
    val className = JavaClassName(examples.resolve("java_basic.txt").readText())
    val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

    val cl =
        URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

    val clz = cl.loadClass("pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint")

    val constructorMethodType = MethodType.methodType(Any::class.java, Array<Any>::class.java)
    val constructorMethodHandle =
        MethodHandles.publicLookup().findStatic(clz, "execute", constructorMethodType)

    val inst = constructorMethodHandle.invoke(arrayOf(1, 2))

    assertThat(inst).isEqualTo("3")
  }
}
