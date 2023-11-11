@file:DependsOn("org.zeroturnaround:zt-exec:1.12")
@file:DependsOn("org.assertj:assertj-core:3.24.2")
@file:DependsOn("com.google.code.gson:gson:2.8.5")

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.zeroturnaround.exec.ProcessExecutor
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

fun String.runCommand(envVars: Map<String, String> = emptyMap()) {
  ProcessExecutor().commandSplit(this)
    .environment(envVars)
    .exitValueNormal()
    .redirectOutput(System.out)
    .redirectError(System.err)
    .timeout(10, TimeUnit.MINUTES)
    .execute()
}

fun String.runCommandAndReturnOutput(envVars: Map<String, String> = emptyMap()): String {
  return ProcessExecutor().commandSplit(this)
    .environment(envVars)
    .exitValueNormal()
    .readOutput(true)
    .redirectOutputAlsoTo(System.out)
    .redirectErrorAlsoTo(System.err)
    .timeout(10, TimeUnit.MINUTES)
    .execute()
    .outputUTF8()
}

"pulumi login --local".runCommand()
"pulumi stack init dev".runCommand(mapOf("USE_PULUMI_AWS" to "true", "PULUMI_CONFIG_PASSPHRASE" to "password"))
"pulumi stack select dev".runCommand()
"pulumi up --yes".runCommand(mapOf("USE_PULUMI_AWS" to "true", "PULUMI_CONFIG_PASSPHRASE" to "password"))
val result = "pulumi stack output --json".runCommandAndReturnOutput(mapOf("PULUMI_CONFIG_PASSPHRASE" to "password"))

try {
  val jsonObject: JsonObject = JsonParser().parse(result).asJsonObject
  assertThat(jsonObject.getAsJsonPrimitive("lambda1").asString).isEqualTo("\"Info from lambda 1\"")
  assertThat(jsonObject.getAsJsonPrimitive("lambda2").asString).isEqualTo("\"Info from lambda 2 + input [\\\"Info from lambda 1\\\"]\"")
} finally {
  "pulumi destroy --yes".runCommand(mapOf("PULUMI_CONFIG_PASSPHRASE" to "password"))
  "pulumi stack rm dev -y".runCommand(mapOf("PULUMI_CONFIG_PASSPHRASE" to "password"))
}