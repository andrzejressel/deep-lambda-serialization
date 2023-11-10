// SPDX-License-Identifier: GPL-2.0-or-later
package com.example.project

import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction0

fun main() {
  val lambda: SerializableFunction0<String> =
      object : SerializableFunction0<String>() {
        override fun execute(): String {
          return listOf("hello", "from", "kotlin").joinToString(separator = " ")
        }
      }
  val url = DeepLambdaSerialization.getJar(lambda)
  println("JAR: [$url]")
}
