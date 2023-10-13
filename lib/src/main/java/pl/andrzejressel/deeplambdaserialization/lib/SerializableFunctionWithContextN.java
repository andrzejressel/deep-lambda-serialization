package pl.andrzejressel.deeplambdaserialization.lib;

public abstract class SerializableFunctionWithContextN<CONTEXT> extends SerializableFunction {
  public abstract Object execute(CONTEXT context, Object[] args);
}
