package com.example.project

import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction0
import pl.andrzejressel.sjs.serializator.Serializator
import pl.andrzejressel.sjs.serializator.StringSerializator

fun main() {
    val lambda: SerializableFunction0<String> = object : SerializableFunction0<String>() {
        override fun getReturnSerializator(): Serializator<String> {
            return StringSerializator.INSTANCE
        }

        override fun execute(): String {
            return "test string"
        }
    }
    val url = DeepLambdaSerialization.getJar(lambda.javaClass, lambda)
    println("JAR: [$url]")
}