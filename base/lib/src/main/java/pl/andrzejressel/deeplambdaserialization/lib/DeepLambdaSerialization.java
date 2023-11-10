package pl.andrzejressel.deeplambdaserialization.lib;

import java.net.URL;
import org.jetbrains.annotations.NotNull;

public class DeepLambdaSerialization {

  /**
   * @param serializableFunctionN Serializable function for which URL jar will be returned
   * @return URL to jar where serializableFunctionN is located
   * @throws IllegalArgumentException When jar for given function cannot be found
   */
  @NotNull
  public static URL getJar(SerializableFunctionN<?> serializableFunctionN)
      throws IllegalArgumentException {
    var fileName = NameUtils.getJarName(serializableFunctionN);
    var url = serializableFunctionN.getClass().getResource("/META-INF/" + fileName + ".jar");
    if (url == null) {
      throw new IllegalArgumentException(
          "Cannot find lambda zip for function: " + serializableFunctionN.getClass());
    }
    return url;
  }
}
