// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.lib;

public abstract class SerializableFunctionN<RESULT> extends SerializableFunction {
  public abstract RESULT execute(Object[] args);
}
