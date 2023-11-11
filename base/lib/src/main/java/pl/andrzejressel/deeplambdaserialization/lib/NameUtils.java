// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.lib;

public final class NameUtils {

  public static String getJarName(SerializableFunction f) {
    return getJarName(f.getClass());
  }

  public static String getJarName(Class<?> clz) {
    return getJarName(clz.getName());
  }

  public static String getJarName(ClassName className) {
    return getJarName(className.getJavaClassName());
  }

  public static String getJarName(String clz) {
    String illegalCharsPattern = "[\\\\/:*?\"<>|]";
    return clz.replaceAll(illegalCharsPattern, "");
  }
}
