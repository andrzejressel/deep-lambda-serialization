// SPDX-License-Identifier: GPL-3.0-or-later
package com.test.lib;

import pl.andrzejressel.deeplambdaserialization.lib.*;

public class SharedLib {
  public static SerializableFunction0<String> lambda = new SerializableFunction0<String>() {
    @Override
    public String execute() {
      return "hello from lambda";
    }
  };
}
