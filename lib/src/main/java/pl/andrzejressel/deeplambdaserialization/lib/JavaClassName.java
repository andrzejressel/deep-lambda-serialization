package pl.andrzejressel.deeplambdaserialization.lib;

public class JavaClassName implements ClassName {
  private final String className;

  public JavaClassName(SerializableFunction f) {
    this(f.getClass());
  }

  public JavaClassName(Class<?> clz) {
    this(clz.getName());
  }

  public JavaClassName(String className) {
    this.className = className;
  }

  @Override
  public String getJavaClassName() {
    return className;
  }

  @Override
  public String getProguardClassName() {
    return className.replace('.', '/');
  }

  @Override
  public String toString() {
    return "JavaClassName{" + "className='" + className + '\'' + '}';
  }
}
