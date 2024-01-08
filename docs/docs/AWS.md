# AWS support

### Adding dependencies

```kotlin
buildscript {
    dependencies { classpath("pl.andrzejressel.deeplambdaserialization:gradle-plugin:VERSION") }
    dependencies { classpath("pl.andrzejressel.deeplambdaserialization.aws:gradle-plugin:VERSION") }
}

apply<DeepSerializationPlugin>()
apply<DeepSerializationAWSPlugin>()
```

### Declaring lambdas

For now AWS support is only for input lambdas

```kotlin
import pl.andrzejressel.deeplambdaserialization.libkotlin.createInput

object Lambdas {
    val lambda1
        get() = createInput { -> "Hello from lambda" }
}
```


### Using in Pulumi-Kotlin

```kotlin
import pl.andrzejressel.deeplambdaserialization.aws.AWSLib.getArgumentsForAWSLambda

val (file, envVars, handler) = getArgumentsForAWSLambda(Lambdas.lambda1)

val lambda = function("test-function-1") {
    args {
        environment { variables(envVars) }
        role(iamForLambda.arn)
        code(file)
        handler(handler)
        runtime("java17")
        tags("deeplambdaserializationtest" to "true")
    }
}
```