package com.example.project;

import pl.andrzejressel.sjs.serializator.*;
import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction0;

public class Main {

    public static void main(String[] args) {
        var lambda = new SerializableFunction0<String>() {
            @Override
            public String execute() {
                return "test string";
            }
        };

        var url = DeepLambdaSerialization.getJar(lambda);
        System.out.println("JAR: [" + url + "]");
    }

}