package pl.andrzejressel.deeplambdaserialization.serializator.examples;

import org.junit.jupiter.api.Test;
import pl.andrzejressel.deeplambdaserialization.lib.*;

import java.text.MessageFormat;

public class JavaLambdasTest extends AbstractLambdaGeneratorTest {

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
