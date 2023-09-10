package pl.andrzejressel.deeplambdaserialization.serializator.examples

import org.junit.jupiter.api.Test
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction2
import java.text.MessageFormat

class GenerateKotlinLambdas : AbstractLambdaGeneratorTest() {

    @Test
    fun generateKotlinLambda() {
        val tag = "kotlin_basic"
        save(tag, object : SerializableFunction2<Int, Int, String>() {
            override fun execute(integer: Int, integer2: Int): String {
                return MessageFormat.format("{0}", integer + integer2)
            }
        })
    }

}