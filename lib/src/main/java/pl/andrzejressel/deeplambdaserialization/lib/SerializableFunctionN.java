package pl.andrzejressel.deeplambdaserialization.lib;

import java.util.List;
import pl.andrzejressel.sjs.serializator.Serializator;

public abstract class SerializableFunctionN {
  public abstract Serializator<Object> getResultSerializator();

  public abstract List<Serializator<Object>> getArgumentsSerializator();

  public abstract Object execute(Object[] args);
}
