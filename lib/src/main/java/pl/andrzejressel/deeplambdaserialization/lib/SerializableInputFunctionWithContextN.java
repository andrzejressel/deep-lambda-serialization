package pl.andrzejressel.deeplambdaserialization.lib;

import java.nio.ByteBuffer;
import java.util.List;
import pl.andrzejressel.dto.serializator.Serializator;

public abstract class SerializableInputFunctionWithContextN<CONTEXT>
    extends SerializableFunctionWithContextN<CONTEXT> {
  public abstract List<Serializator<Object>> getInputSerializators();

  public final Object execute(CONTEXT context, byte[][] serializedArgs) {
    var args = new Object[serializedArgs.length];
    var serializators = getInputSerializators();
    for (var i = 0; i < serializators.size(); i++) {
      args[i] = serializators.get(i).deserialize(ByteBuffer.wrap(serializedArgs[i]));
    }
    return execute(context, args);
  }
}
