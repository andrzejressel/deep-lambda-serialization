// SPDX-License-Identifier: GPL-2.0-or-later
package pl.andrzejressel.deeplambdaserialization.serializator

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader
import kotlin.io.path.readText
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import pl.andrzejressel.deeplambdaserialization.lib.JavaClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils
import pl.andrzejressel.dto.serializator.IntegerSerializator
import pl.andrzejressel.dto.serializator.StringSerializator

class ContextEntrypointTest {

  private val serializedTwo = IntegerSerializator.INSTANCE.serialize(2).array()
  private val serializedThree = StringSerializator.INSTANCE.serialize("3").array()

  @Test
  fun serializableFunctionShouldHave1Function() {
    val examples = BuildInfo.location.resolve("build/examples")
    val className =
        JavaClassName(examples.resolve("java_context_serializablefunction.txt").readText())
    val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

    val cl =
        URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

    val clz = cl.loadClass("pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint")

    val executeMethod = getExecuteMethod(clz)
    assertThat(executeMethod.invoke(1, arrayOf(2))).isEqualTo("3")

    try {
      getSerializedInputExecuteMethod(clz)
      fail("Serialized input method is available, but it shouldn't")
    } catch (_: NoSuchMethodException) {}

    try {
      getSerializedOutputMethod(clz)
      fail("Serialized output method is available, but it shouldn't")
    } catch (_: NoSuchMethodException) {}

    try {
      getSerializedInputOutputMethod(clz)
      fail("Serialized input/output method is available, but it shouldn't")
    } catch (_: NoSuchMethodException) {}
  }

  @Test
  fun serializableInputFunctionShouldHave2Functions() {
    val examples = BuildInfo.location.resolve("build/examples")
    val className =
        JavaClassName(examples.resolve("java_context_serializableinputfunction.txt").readText())
    val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

    val cl =
        URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

    val clz = cl.loadClass("pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint")

    val executeMethod = getExecuteMethod(clz)
    assertThat(executeMethod.invoke(1, arrayOf(2))).isEqualTo("3")

    val inputExecuteMethod = getSerializedInputExecuteMethod(clz)
    assertThat(inputExecuteMethod.invoke(1, arrayOf(serializedTwo))).isEqualTo("3")

    try {
      getSerializedOutputMethod(clz)
      fail("Serialized output method is available, but it shouldn't")
    } catch (_: NoSuchMethodException) {}

    try {
      getSerializedInputOutputMethod(clz)
      fail("Serialized input/output method is available, but it shouldn't")
    } catch (_: NoSuchMethodException) {}
  }

  @Test
  fun serializableInputOutputFunctionShouldHave4Functions() {
    val examples = BuildInfo.location.resolve("build/examples")
    val className =
        JavaClassName(
            examples.resolve("java_context_serializableinputoutputfunction.txt").readText())
    val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

    val cl =
        URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

    val clz = cl.loadClass("pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint")

    val executeMethod = getExecuteMethod(clz)
    assertThat(executeMethod.invoke(1, arrayOf(2))).isEqualTo("3")

    val inputExecuteMethod = getSerializedInputExecuteMethod(clz)
    assertThat(inputExecuteMethod.invoke(1, arrayOf(serializedTwo))).isEqualTo("3")

    val serializedOutputExecuteMethod = getSerializedOutputMethod(clz)
    assertThat(serializedOutputExecuteMethod.invoke(1, arrayOf(2))).isEqualTo(serializedThree)

    val serializedInputOutputExecuteMethod = getSerializedInputOutputMethod(clz)
    assertThat(serializedInputOutputExecuteMethod.invoke(1, arrayOf(serializedTwo)))
        .isEqualTo(serializedThree)
  }

  private fun getExecuteMethod(clz: Class<*>): MethodHandle {
    val constructorMethodType =
        MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java)
    return MethodHandles.publicLookup().findStatic(clz, "execute", constructorMethodType)
  }

  private fun getSerializedInputExecuteMethod(clz: Class<*>): MethodHandle {
    val constructorMethodType =
        MethodType.methodType(Any::class.java, Any::class.java, Array<ByteArray>::class.java)
    return MethodHandles.publicLookup().findStatic(clz, "execute", constructorMethodType)
  }

  private fun getSerializedOutputMethod(clz: Class<*>): MethodHandle {
    val constructorMethodType =
        MethodType.methodType(ByteArray::class.java, Any::class.java, Array<Any>::class.java)
    return MethodHandles.publicLookup()
        .findStatic(clz, "executeAndSerialize", constructorMethodType)
  }

  private fun getSerializedInputOutputMethod(clz: Class<*>): MethodHandle {
    val constructorMethodType =
        MethodType.methodType(ByteArray::class.java, Any::class.java, Array<ByteArray>::class.java)
    return MethodHandles.publicLookup()
        .findStatic(clz, "executeAndSerialize", constructorMethodType)
  }
}
