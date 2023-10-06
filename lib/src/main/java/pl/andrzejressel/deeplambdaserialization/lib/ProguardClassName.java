package pl.andrzejressel.deeplambdaserialization.lib;

public class ProguardClassName implements ClassName {
    private final String className;

    public ProguardClassName(String className) {
        this.className = className;
    }

    @Override
    public String getJavaClassName() {
        return className.replace('/', '.');
    }

    @Override
    public String getProguardClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "ProguardClassName{" +
                "className='" + className + '\'' +
                '}';
    }
}