package pl.andrzejressel.deeplambdaserialization.lib;

import pl.andrzejressel.dto.serializator.Serializator;

public abstract class SerializableInputOutputFunctionWithContextN<CONTEXT>
    extends SerializableInputFunctionWithContextN<CONTEXT> {
  public abstract Serializator<Object> getOutputSerializator();

  public byte[] executeAndSerialize(CONTEXT context, Object[] args) {
    return getOutputSerializator().serialize(execute(context, args)).array();
  }

  public byte[] executeAndSerialize(CONTEXT context, byte[][] serializedArgs) {
    return getOutputSerializator().serialize(execute(context, serializedArgs)).array();
  }
}
