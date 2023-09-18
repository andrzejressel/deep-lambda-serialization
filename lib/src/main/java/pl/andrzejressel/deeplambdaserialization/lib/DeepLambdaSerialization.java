package pl.andrzejressel.deeplambdaserialization.lib;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class DeepLambdaSerialization {

    /**
     *
     * @param clz Class in jar/subproject where serializableFunctionN is defined. Usually it will be caller.
     * @param serializableFunctionN Serializable function for which URL jar will be returned
     * @return URL to jar where serializableFunctionN is located
     * @throws IllegalArgumentException When jar for given function cannot be found
     */
    @NotNull
    public static URL getJar(Class<?> clz, SerializableFunctionN serializableFunctionN) throws IllegalArgumentException {
        var fileName = NameUtils.getJarName(serializableFunctionN);
        var url = clz.getResource("/META-INF/" + fileName + ".jar");
        if (url == null) {
            throw new IllegalArgumentException("Cannot find lambda zip for function: " + serializableFunctionN.getClass());
        }
        return url;
    }

}
