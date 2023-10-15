package pl.andrzejressel.deeplambdaserialization.serializator.examples;

import java.text.MessageFormat;
import org.junit.jupiter.api.Test;
import pl.andrzejressel.deeplambdaserialization.lib.*;
import pl.andrzejressel.dto.serializator.IntegerSerializator;
import pl.andrzejressel.dto.serializator.Serializator;
import pl.andrzejressel.dto.serializator.StringSerializator;

@SuppressWarnings("NewClassNamingConvention")
public class GenerateEntrypointLambdasWithContext extends AbstractLambdaGeneratorTest {

  @Test
  public void generateSerializableFunction() {
    var tag = "java_context_serializablefunction";

    save(tag, new SerializableFunctionWithContext1<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }
    });
  }

  @Test
  public void generateSerializableInputFunction() {
    var tag = "java_context_serializableinputfunction";

    save(tag, new SerializableInputFunctionWithContext1<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }

      @Override
      protected Serializator<Integer> getArg1Serializator() {
        return IntegerSerializator.INSTANCE;
      }
    });
  }

  @Test
  public void generateSerializableInputOutputFunction() {
    var tag = "java_context_serializableinputoutputfunction";

    save(tag, new SerializableInputOutputFunctionWithContext1<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }

      @Override
      public Serializator<String> getOutputSerializator() {
        return StringSerializator.INSTANCE;
      }

      @Override
      protected Serializator<Integer> getArg1Serializator() {
        return IntegerSerializator.INSTANCE;
      }
    });
  }
}
