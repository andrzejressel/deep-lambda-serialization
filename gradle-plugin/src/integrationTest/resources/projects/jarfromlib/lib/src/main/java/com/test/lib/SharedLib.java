package com.test.lib;

import pl.andrzejressel.deeplambdaserialization.lib.*;
import pl.andrzejressel.sjs.serializator.Serializator;
import pl.andrzejressel.sjs.serializator.StringSerializator;

public class SharedLib {
    public static SerializableFunction0<String> lambda = new SerializableFunction0<String>() {
        @Override
        public String execute() {
            return "hello from lambda";
        }

        @Override
        public Serializator<String> getReturnSerializator() {
            return StringSerializator.INSTANCE;
        }
    };
}