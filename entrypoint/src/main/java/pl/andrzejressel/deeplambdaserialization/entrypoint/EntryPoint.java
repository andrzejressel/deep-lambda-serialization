package pl.andrzejressel.deeplambdaserialization.entrypoint;

public class EntryPoint {
  public static Object execute(Object[] args) {
    throwError();
    return null;
  }

  public static Object execute(Object context, Object[] args) {
    throwError();
    return null;
  }

  public static Object execute(byte[][] args) {
    throwError();
    return null;
  }

  public static Object execute(Object context, byte[][] args) {
    throwError();
    return null;
  }

  public static byte[] executeAndSerialize(Object[] args) {
    throwError();
    return null;
  }

  public static byte[] executeAndSerialize(Object context, Object[] args) {
    throwError();
    return null;
  }

  public static byte[] executeAndSerialize(byte[][] args) {
    throwError();
    return null;
  }

  public static byte[] executeAndSerialize(Object context, byte[][] args) {
    throwError();
    return null;
  }

  private static void throwError() {
    throw new IllegalStateException("entrypoint should be in 'provided' scope");
  }
}
