// SPDX-License-Identifier: GPL-3.0-or-later
import pl.andrzejressel.deeplambdaserialization.libkotlin.createInput

object Lambdas {

  val lambda1
    get() = createInput { -> "Info from lambda 1" }

  val lambda2
    get() = createInput { input: String -> "Info from lambda 2 + input [$input]" }
}
