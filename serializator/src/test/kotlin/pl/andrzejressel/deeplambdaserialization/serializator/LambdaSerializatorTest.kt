package pl.andrzejressel.deeplambdaserialization.serializator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.andrzejressel.deeplambdaserialization.lib.JavaClassName
import pl.andrzejressel.deeplambdaserialization.lib.NameUtils
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunctionN
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader
import kotlin.io.path.readText

class LambdaSerializatorTest {

    @Test
    fun shouldMinimizeKotlinLambda() {
        val examples = BuildInfo.location.resolve("build/examples")
        val className = JavaClassName(examples.resolve("kotlin_basic.txt").readText())
        val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

        val cl = URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

        val clz = cl.loadClass(className.javaClassName)

        val constructorMethodType = MethodType.methodType(Void.TYPE)
        val constructorMethodHandle = MethodHandles.publicLookup().findConstructor(clz, constructorMethodType)

        val inst = constructorMethodHandle.invoke() as SerializableFunctionN

        assertThat(inst.execute(arrayOf(1, 2))).isEqualTo("3")
    }

    @Test
    fun shouldMinimizeJavaLambda() {
        val examples = BuildInfo.location.resolve("build/examples")
        val className = JavaClassName(examples.resolve("java_basic.txt").readText())
        val jar = examples.resolve("jars").resolve("${NameUtils.getJarName(className)}.jar")

        val cl = URLClassLoader("test", arrayOf(jar.toFile().toURI().toURL()), this::class.java.classLoader)

        val clz = cl.loadClass(className.javaClassName)

        val constructorMethodType = MethodType.methodType(Void.TYPE)
        val constructorMethodHandle = MethodHandles.publicLookup().findConstructor(clz, constructorMethodType)

        val inst = constructorMethodHandle.invoke() as SerializableFunctionN

        assertThat(inst.execute(arrayOf(1, 2))).isEqualTo("3")
    }

}