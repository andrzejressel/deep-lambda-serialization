package pl.andrzejressel.deeplambdaserialization.entrypoint;

public class EntryPoint {
  public static Object execute(Object[] var0) {
    throwError();
    return null;
  }

  public static Object execute(byte[][] var0) {
    throwError();
    return null;
  }

  public static byte[] executeAndSerialize(Object[] var0) {
    throwError();
    return null;
  }

  public static byte[] executeAndSerialize(byte[][] var0) {
    throwError();
    return null;
  }

  private static void throwError() {
    throw new IllegalStateException("entrypoint should be in 'provided' scope");
  }
}
