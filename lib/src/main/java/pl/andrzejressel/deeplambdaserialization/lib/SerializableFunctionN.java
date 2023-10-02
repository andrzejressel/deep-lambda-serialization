package pl.andrzejressel.deeplambdaserialization.lib;

import pl.andrzejressel.sjs.serializator.Serializator;

import java.util.List;

public abstract class SerializableFunctionN {
    public abstract Serializator<Object> getResultSerializator();
    public abstract List<Serializator<Object>> getArgumentsSerializator();
    public abstract Object execute(Object[] args);
}
