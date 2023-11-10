// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.aws

import com.pulumi.asset.FileArchive
import com.pulumi.core.Output
import java.nio.file.Files
import java.util.Base64
import kotlin.io.path.absolutePathString
import pl.andrzejressel.deeplambdaserialization.lib.*
import pl.andrzejressel.dto.serializator.Serializator

@Suppress("UNCHECKED_CAST")
abstract class AbstractAWSLibKT {

  companion object {
    private val HANDLER = "pl.andrzejressel.deeplambdaserialization.aws.handler.Handler"

    @JvmStatic
    protected fun <RET> handleFunctionN(
        f: SerializableInputFunctionN<RET>,
        args: List<Output<*>>
    ): LambdaArguments {

      val anyArgs: List<Output<Any>> = args as List<Output<Any>>

      val envVars =
          Output.all(anyArgs).applyValue { list ->
            list
                .zip(f.inputSerializators)
                .mapIndexed { index, (obj, serializator) ->
                  serializator as Serializator<Any>

                  "ARG_${index + 1}" to
                      Base64.getEncoder().encodeToString(serializator.serialize(obj).array())
                }
                .toMap()
          }

      val file = DeepLambdaSerialization.getJar(f)

      val dir = Files.createTempDirectory("deeplambdaserialization")

      val fsJar =
          file.openStream().use {
            Files.copy(it, dir.resolve("test.jar"))
            dir.resolve("test.jar")
          }

      return LambdaArguments(FileArchive(fsJar.absolutePathString()), envVars, HANDLER)
    }
  }

  data class LambdaArguments(
      val code: FileArchive,
      val envVars: Output<Map<String, String>>,
      val handlerClass: String
  )
}
