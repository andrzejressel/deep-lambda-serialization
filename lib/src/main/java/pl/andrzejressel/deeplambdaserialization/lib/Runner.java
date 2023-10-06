package pl.andrzejressel.deeplambdaserialization.lib;

import java.nio.ByteBuffer;

public class Runner {

  public static Object runObject(SerializableFunctionN serializableFunctionN, Object[] args) {
    return serializableFunctionN.execute(args);
  }

  public static Object runObject(
      SerializableFunctionN serializableFunctionN, byte[][] serializedArgs) {
    var args = new Object[serializedArgs.length];
    for (int i = 0; i < serializableFunctionN.getArgumentsSerializator().size(); i++) {
      args[i] =
          serializableFunctionN
              .getArgumentsSerializator()
              .get(i)
              .deserialize(ByteBuffer.wrap(serializedArgs[i]));
    }
    return runObject(serializableFunctionN, args);
  }

  public static byte[] runSerialized(SerializableFunctionN serializableFunctionN, Object[] args) {
    return serializableFunctionN
        .getResultSerializator()
        .serializeFlatten(runObject(serializableFunctionN, args))
        .array();
  }

  public static byte[] runSerialized(
      SerializableFunctionN serializableFunctionN, byte[][] serializedArgs) {
    return serializableFunctionN
        .getResultSerializator()
        .serializeFlatten(runObject(serializableFunctionN, serializedArgs))
        .array();
  }
}
