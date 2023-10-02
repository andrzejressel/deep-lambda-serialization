package pl.andrzejressel.deeplambdaserialization.serializator.examples;

import org.junit.jupiter.api.Test;
import pl.andrzejressel.deeplambdaserialization.lib.*;
import pl.andrzejressel.sjs.serializator.IntegerSerializator;
import pl.andrzejressel.sjs.serializator.Serializator;
import pl.andrzejressel.sjs.serializator.StringSerializator;

import java.text.MessageFormat;

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

            @Override
            public Serializator<String> getReturnSerializator() {
                return StringSerializator.INSTANCE;
            }

            @Override
            protected Serializator<Integer> getASerializator() {
                return IntegerSerializator.INSTANCE;
            }

            @Override
            protected Serializator<Integer> getBSerializator() {
                return IntegerSerializator.INSTANCE;
            }
        });
    }

}
