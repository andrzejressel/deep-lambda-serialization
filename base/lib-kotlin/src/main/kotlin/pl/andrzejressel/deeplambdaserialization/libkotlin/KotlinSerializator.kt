// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.libkotlin

import java.nio.ByteBuffer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import pl.andrzejressel.dto.serializator.Serializator

class KotlinSerializator<T>(private val ktSerializator: KSerializer<T>) : Serializator<T> {

  companion object {
    inline fun <reified T> create(): KotlinSerializator<T> {
      val serializator = Json.serializersModule.serializer<T>()
      return KotlinSerializator(serializator)
    }
  }

  override fun deserialize(bb: ByteBuffer): T {
    val s = String(bb.array())
    return Json.decodeFromString(ktSerializator, s)
  }

  override fun serialize(t: T): ByteBuffer {
    val s = Json.encodeToString(ktSerializator, t)
    return ByteBuffer.wrap(s.encodeToByteArray())
  }
}
