package pl.andrzejressel.deeplambdaserialization.lib;

import pl.andrzejressel.dto.serializator.Serializator;

public abstract class SerializableInputOutputFunctionN<RESULT>
    extends SerializableInputFunctionN<RESULT> {
  public abstract Serializator<RESULT> getOutputSerializator();

  public byte[] executeAndSerialize(Object[] args) {
    return getOutputSerializator().serialize(execute(args)).array();
  }

  public byte[] executeAndSerialize(byte[][] serializedArgs) {
    return getOutputSerializator().serialize(execute(serializedArgs)).array();
  }
}
