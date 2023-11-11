// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.serializator.examples;

import java.text.MessageFormat;
import org.junit.jupiter.api.Test;
import pl.andrzejressel.deeplambdaserialization.lib.*;

@SuppressWarnings("NewClassNamingConvention")
public class GenerateJavaLambdas extends AbstractLambdaGeneratorTest {

  @Test
  public void generateJavaLambda() {
    var tag = "java_basic";

    save(tag, new SerializableFunction2<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }
    });
  }
}
