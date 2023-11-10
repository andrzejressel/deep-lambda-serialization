// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.aws.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64
import pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint

class StreamHandler : RequestStreamHandler {
  override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
    val args = getArgs().toTypedArray()
    val ioContext = LambdaIOContext(input, output, context)
    EntryPoint.execute(ioContext, args)
  }

  private fun getArgs(): List<ByteArray> {
    val args = mutableListOf<ByteArray>()
    var i = 0

    while (true) {
      i++
      val base64Arg = System.getenv("ARG_$i") ?: return args
      args.add(Base64.getDecoder().decode(base64Arg))
    }
  }
}
