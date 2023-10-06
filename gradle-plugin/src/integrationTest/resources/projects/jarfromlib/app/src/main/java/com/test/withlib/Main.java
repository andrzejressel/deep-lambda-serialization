package com.test.withlib;

import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization;
import com.test.lib.SharedLib;

public class Main {

    public static void main(String[] args) {
        var url = DeepLambdaSerialization.getJar(SharedLib.lambda);
        System.out.println("JAR: [" + url + "]");
    }

}