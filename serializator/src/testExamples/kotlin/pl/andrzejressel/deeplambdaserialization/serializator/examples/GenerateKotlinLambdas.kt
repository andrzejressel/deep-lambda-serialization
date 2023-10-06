package pl.andrzejressel.deeplambdaserialization.serializator.examples

import java.text.MessageFormat
import org.junit.jupiter.api.Test
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction2
import pl.andrzejressel.sjs.serializator.IntegerSerializator
import pl.andrzejressel.sjs.serializator.Serializator
import pl.andrzejressel.sjs.serializator.StringSerializator

class GenerateKotlinLambdas : AbstractLambdaGeneratorTest() {

  @Test
  fun generateKotlinLambda() {
    val tag = "kotlin_basic"
    save(
        tag,
        object : SerializableFunction2<Int, Int, String>() {
          override fun execute(integer: Int, integer2: Int): String {
            return MessageFormat.format("{0}", integer + integer2)
          }

          override fun getReturnSerializator(): Serializator<String> {
            return StringSerializator.INSTANCE
          }

          override fun getASerializator(): Serializator<Int> {
            return IntegerSerializator.INSTANCE
          }

          override fun getBSerializator(): Serializator<Int> {
            return IntegerSerializator.INSTANCE
          }
        })
  }
}
