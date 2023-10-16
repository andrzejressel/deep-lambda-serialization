package pl.andrzejressel.deeplambdaserialization.lib;

public abstract class SerializableFunctionN<RESULT> extends SerializableFunction {
  public abstract RESULT execute(Object[] args);
}
