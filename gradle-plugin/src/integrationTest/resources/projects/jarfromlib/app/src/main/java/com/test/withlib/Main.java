package com.test.withlib;

import com.test.lib.SharedLib;
import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization;

public class Main {

  public static void main(String[] args) {
    var url = DeepLambdaSerialization.getJar(SharedLib.lambda);
    System.out.println("JAR: [" + url + "]");
  }
}
