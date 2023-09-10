package pl.andrzejressel.deeplambdaserialization.lib;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class NameUtils {

    public static String getJarName(SerializableFunctionN f) {
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
