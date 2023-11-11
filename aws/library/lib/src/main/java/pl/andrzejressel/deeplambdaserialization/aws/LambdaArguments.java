// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.aws;

import com.pulumi.asset.FileArchive;
import com.pulumi.core.Output;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class LambdaArguments {

  @NotNull
  public final FileArchive code;

  @NotNull
  public final Output<Map<String, String>> envVars;

  @NotNull
  public final String handlerClass;

  public LambdaArguments(
      @NotNull FileArchive code,
      @NotNull Output<Map<String, String>> envVars,
      @NotNull String handlerClass) {
    this.code = code;
    this.envVars = envVars;
    this.handlerClass = handlerClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LambdaArguments that = (LambdaArguments) o;
    return Objects.equals(code, that.code)
        && Objects.equals(envVars, that.envVars)
        && Objects.equals(handlerClass, that.handlerClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, envVars, handlerClass);
  }

  @NotNull
  public FileArchive component1() {
    return code;
  }

  @NotNull
  public Output<Map<String, String>> component2() {
    return envVars;
  }

  @NotNull
  public String component3() {
    return handlerClass;
  }
}
