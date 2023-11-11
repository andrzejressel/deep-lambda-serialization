// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.aws.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.util.Base64
import pl.andrzejressel.deeplambdaserialization.entrypoint.EntryPoint

class Handler : RequestHandler<Any, Any> {
  override fun handleRequest(event: Any, context: Context): Any {
    return EntryPoint.execute(getArgs().toTypedArray())
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
