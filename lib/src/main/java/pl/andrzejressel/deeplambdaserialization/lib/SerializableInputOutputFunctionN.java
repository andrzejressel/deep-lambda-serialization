package pl.andrzejressel.deeplambdaserialization.lib;

import pl.andrzejressel.sjs.serializator.Serializator;

public abstract class SerializableInputOutputFunctionN extends SerializableInputFunctionN {
  public abstract Serializator<Object> getOutputSerializator();

  public byte[] executeAndSerialize(Object[] args) {
    return getOutputSerializator().serialize(execute(args)).array();
  }

  public byte[] executeAndSerialize(byte[][] serializedArgs) {
    return getOutputSerializator().serialize(execute(serializedArgs)).array();
  }
}
