package pl.andrzejressel.deeplambdaserialization.aws;

import com.pulumi.asset.FileArchive;
import com.pulumi.core.Output;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableInputFunctionN;
import pl.andrzejressel.dto.serializator.Serializator;

class AbstractAWSLib {

  private static final String HANDLER =
      "pl.andrzejressel.deeplambdaserialization.aws.handler.Handler";

  protected static <RET> LambdaArguments handleFunctionN(
      SerializableInputFunctionN<RET> f, List<Output<?>> args) {

    @SuppressWarnings("unchecked")
    var castedArgs = args.stream().map(t -> (Output<Object>) t).collect(Collectors.toList());

    var envVars = Output.all(castedArgs).applyValue((list) -> {
      Map<String, String> envVarsMap = new HashMap<>();

      for (int i = 0; i < list.size(); i++) {
        var argument = (Object) list.get(i);
        @SuppressWarnings("unchecked")
        var serializator = (Serializator<Object>) f.getInputSerializators().get(i);

        envVarsMap.put(
            "ARG_" + (i + 1),
            Base64.getEncoder().encodeToString(serializator.serialize(argument).array()));
      }

      return envVarsMap;
    });

    var file = DeepLambdaSerialization.getJar(f);
    try {
      var dir = Files.createTempDirectory("deeplambdaserialization");
      Files.copy(file.openStream(), dir.resolve("test.jar"));
      var jarLocation = dir.resolve("test.jar");
      return new LambdaArguments(
          new FileArchive(jarLocation.toAbsolutePath().toString()), envVars, HANDLER);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
