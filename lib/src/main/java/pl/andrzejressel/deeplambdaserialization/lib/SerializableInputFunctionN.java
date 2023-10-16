package pl.andrzejressel.deeplambdaserialization.lib;

import java.nio.ByteBuffer;
import java.util.List;
import pl.andrzejressel.dto.serializator.Serializator;

public abstract class SerializableInputFunctionN<RESULT> extends SerializableFunctionN<RESULT> {
  public abstract List<Serializator<?>> getInputSerializators();

  public final RESULT execute(byte[][] serializedArgs) {
    var args = new Object[serializedArgs.length];
    var serializators = getInputSerializators();
    for (var i = 0; i < serializators.size(); i++) {
      args[i] = serializators.get(i).deserialize(ByteBuffer.wrap(serializedArgs[i]));
    }
    return execute(args);
  }
}
