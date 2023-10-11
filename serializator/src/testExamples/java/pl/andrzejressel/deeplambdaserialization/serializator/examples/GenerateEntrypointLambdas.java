package pl.andrzejressel.deeplambdaserialization.serializator.examples;

import java.text.MessageFormat;
import org.junit.jupiter.api.Test;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction2;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableInputFunction2;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableInputOutputFunction2;
import pl.andrzejressel.dto.serializator.IntegerSerializator;
import pl.andrzejressel.dto.serializator.Serializator;
import pl.andrzejressel.dto.serializator.StringSerializator;

@SuppressWarnings("NewClassNamingConvention")
public class GenerateEntrypointLambdas extends AbstractLambdaGeneratorTest {

  @Test
  public void generateSerializableFunction() {
    var tag = "java_serializablefunction";

    save(tag, new SerializableFunction2<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }
    });
  }

  @Test
  public void generateSerializableInputFunction() {
    var tag = "java_serializableinputfunction";

    save(tag, new SerializableInputFunction2<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }

      @Override
      protected Serializator<Integer> getArg1Serializator() {
        return IntegerSerializator.INSTANCE;
      }

      @Override
      protected Serializator<Integer> getArg2Serializator() {
        return IntegerSerializator.INSTANCE;
      }
    });
  }

  @Test
  public void generateSerializableInputOutputFunction() {
    var tag = "java_serializableinputoutputfunction";

    save(tag, new SerializableInputOutputFunction2<Integer, Integer, String>() {
      @Override
      public String execute(Integer integer, Integer integer2) {
        return MessageFormat.format("{0}", integer + integer2);
      }

      @Override
      public Serializator<String> getReturnSerializator() {
        return StringSerializator.INSTANCE;
      }

      @Override
      protected Serializator<Integer> getArg1Serializator() {
        return IntegerSerializator.INSTANCE;
      }

      @Override
      protected Serializator<Integer> getArg2Serializator() {
        return IntegerSerializator.INSTANCE;
      }
    });
  }
}
