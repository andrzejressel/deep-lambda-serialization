// SPDX-License-Identifier: GPL-2.0-or-later
import com.pulumi.aws.iam.kotlin.IamFunctions.getRole
import com.pulumi.aws.iam.kotlin.outputs.GetRoleResult
import com.pulumi.aws.lambda.kotlin.function
import com.pulumi.aws.lambda.kotlin.invocation
import com.pulumi.core.Output
import com.pulumi.kotlin.Pulumi
import pl.andrzejressel.deeplambdaserialization.aws.AWSLib.getArgumentsForAWSLambda

fun main() {
  Pulumi.run { ctx ->
    val iamForLambda = getRole("LambdaRole")

    val result1 = createLambda1(iamForLambda)
    val result2 = createLambda2(iamForLambda, result1)

    ctx.export("lambda1", result1).export("lambda2", result2)
  }
}

private suspend fun createLambda1(iamForLambda: GetRoleResult): Output<String> {
  val (file, envVars, handler) = getArgumentsForAWSLambda(Lambdas.lambda1)

  val lambda =
      function("test-function-1") {
        args {
          environment { variables(envVars) }
          role(iamForLambda.arn)
          code(file)
          handler(handler)
          runtime("java17")
          tags("deeplambdaserializationtest" to "true")
        }
      }

  val invocation =
      invocation("test-function-1-invocation") {
        args {
          functionName(lambda.name)
          input("{}")
          triggers(
              lambda.lastModified.applyValue { lastModified ->
                mapOf("lastModified" to lastModified)
              })
        }
      }

  return invocation.result
}

private suspend fun createLambda2(
    iamForLambda: GetRoleResult,
    result: Output<String>
): Output<String> {
  val (file, envVars, handler) = getArgumentsForAWSLambda(Lambdas.lambda2, result)

  val lambda =
      function("test-function-2") {
        args {
          environment { variables(envVars) }
          role(iamForLambda.arn)
          code(file)
          handler(handler)
          runtime("java17")
          tags("deeplambdaserializationtest" to "true")
        }
      }

  val invocation =
      invocation("test-function-2-invocation") {
        args {
          functionName(lambda.name)
          input("{}")
          triggers(
              lambda.lastModified.applyValue { lastModified ->
                mapOf("lastModified" to lastModified)
              })
        }
      }

  return invocation.result
}
