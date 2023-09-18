package com.example.project;

import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction0;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunctionN;

public class Main {

    public static void main(String[] args) {
        var lambda = new SerializableFunction0<String>() {
            @Override
            public String execute() {
                return "test string";
            }
        };

        var url = DeepLambdaSerialization.getJar(Main.class, lambda);
        System.out.println(url);
    }

}