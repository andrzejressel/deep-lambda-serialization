// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.lib;

public abstract class SerializableFunctionWithContextN<RESULT, CONTEXT>
    extends SerializableFunction {
  public abstract RESULT execute(CONTEXT context, Object[] args);
}
