package com.test.withlib;

import com.test.lib.SharedLib;
import pl.andrzejressel.deeplambdaserialization.lib.DeepLambdaSerialization;
import pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction0;
import pl.andrzejressel.dto.serializator.*;

public class Main {

  public static void main(String[] args) {
    var lambda = new SerializableFunction0<String>() {
      @Override
      public String execute() {
        return SharedLib.getString();
      }
    };

    var url = DeepLambdaSerialization.getJar(lambda);
    System.out.println("JAR: [" + url + "]");
  }
}
