# Introduction

### Types of lambdas

#### Normal lambda

Interface:
```
public abstract class SerializableFunctionNumberOfArguments<A, B, ..., RET>
extends SerializableFunctionN<RET> {
    public abstract RET execute(A a, B b, ...);
}
```

Used when input, lambda itself and output reside on the same JVM. Current usecases are unknown and this type may be removed later.

#### Input lambda

Interface:
```
public abstract class SerializableInputFunctionNumberOfArguments<A, B, ..., RET>
extends SerializableInputFunctionN<RET> {
    public abstract RET execute(A a, B b, ...);

    protected abstract Serializator<A> getArg1Serializator();
     
    protected abstract Serializator<B> getArg2Serializator();

...
}
```

Used when input is on one JVM, but lambda and output are used on another one. Current use case is AWS lambda with Pulumi - inputs are serialized on local machine and lambda with output are used in Lambda VM.

#### Input/Output lambda

Interface:
```
public abstract class SerializableInputOutputFunctionNumberOfArguments<A, B, ..., RET>
extends SerializableInputFunctionN<RET> {
    public abstract RET execute(A a, B b, ...);

    public abstract Serializator<RESULT> getOutputSerializator();
    
    protected abstract Serializator<A> getArg1Serializator();
        
    protected abstract Serializator<B> getArg2Serializator();

...
}
```

Used when input is on one JVM, lambda is invoked on second and output is used on third. Should be used when creating distributed system. First system send lambda with serialized input to second system and receives serialized output.

#### Lambdas with context

All the above lambda types contain also type with suffix `WithContext`. Context is object that reside on VM that invokes the lambda and does not take part in any serialization process. Use case for it is `InputStream` and `OutputStream` arguments for AWS lambda.

### Serialization

Deep lambda serialization uses minimization to create the smallest self contained lambda. Due to that serialization that does not use reflection must be used.

#### Java
I couldn't find any library that can serialize code without using reflection so I've created simple library here: [https://github.com/andrzejressel/dto-serializator](https://github.com/andrzejressel/dto-serializator)

#### Kotlin
For Kotlin `kotlinx-serialization-json` is used. Thanks to instrinsics and `inline` functions serializator initialization is in compile time.