// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.serializator.examples

import java.text.MessageFormat
import org.junit.jupiter.api.Test
import pl.andrzejressel.deeplambdaserialization.libkotlin.create
import pl.andrzejressel.deeplambdaserialization.libkotlin.createInput
import pl.andrzejressel.deeplambdaserialization.libkotlin.createInputOutput

class GenerateKotlinLambdas : AbstractLambdaGeneratorTest() {

  @Test
  fun generateBasicKotlinLambda() {

    val tag = "kotlin_basic"
    save(
        tag,
        create { integer: Int, integer2: Int -> MessageFormat.format("{0}", integer + integer2) })
  }

  @Test
  fun generateKotlinLambda() {

    val tag = "kotlin_serializablefunction"
    save(
        tag,
        create { integer: Int, integer2: Int -> MessageFormat.format("{0}", integer + integer2) })
  }

  @Test
  fun generateKotlinInputLambda() {

    val tag = "kotlin_serializableinputfunction"
    save(
        tag,
        createInput { integer: Int, integer2: Int ->
          MessageFormat.format("{0}", integer + integer2)
        })
  }

  @Test
  fun generateKotlinInputOutputLambda() {

    val tag = "kotlin_serializableinputoutputfunction"
    save(
        tag,
        createInputOutput { integer: Int, integer2: Int ->
          MessageFormat.format("{0}", integer + integer2)
        })
  }
}
