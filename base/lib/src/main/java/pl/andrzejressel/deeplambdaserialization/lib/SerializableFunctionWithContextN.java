package pl.andrzejressel.deeplambdaserialization.lib;

public abstract class SerializableFunctionWithContextN<RESULT, CONTEXT>
    extends SerializableFunction {
  public abstract RESULT execute(CONTEXT context, Object[] args);
}
