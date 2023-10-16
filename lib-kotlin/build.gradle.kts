import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

plugins {
  jacoco
  alias(libs.plugins.kotlin)
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
  kotlin("plugin.serialization") version "1.9.10"
}

buildscript {
  dependencies {
    classpath("com.squareup:kotlinpoet:1.14.2")
    classpath("com.facebook:ktfmt:0.46")
  }
}

repositories { mavenCentral() }

dependencies {
  api(project(":lib"))
  compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

mavenPublishing {
  coordinates(mvnGroupId, mvnArtifactId, mvnVersion)

  pom {
    licenses {
      license {
        name = "Gnu Lesser General Public License"
        url = "http://www.gnu.org/licenses/lgpl.txt"
        distribution = "http://www.gnu.org/licenses/lgpl.txt"
      }
    }
  }
}

val generateSerializableFunction by
    tasks.registering {
      outputs.dir(layout.buildDirectory.dir("generated/sources/serializable_function_kt"))
      doLast {
        val dir = outputs.files.single().toPath()
        val alphabet = 'A'..'Z'

        val functions =
            (0..22).flatMap { i ->
              val typeVariables = alphabet.take(i).map { TypeVariableName(it.toString()) }
              val retTypeVariable = TypeVariableName("RET")
              val serializableFunctionResult =
                  ClassName.bestGuess(
                          "pl.andrzejressel.deeplambdaserialization.lib.SerializableFunction${i}")
                      .parameterizedBy(typeVariables + retTypeVariable)
              val serializableInputFunctionResult =
                  ClassName.bestGuess(
                          "pl.andrzejressel.deeplambdaserialization.lib.SerializableInputFunction${i}")
                      .parameterizedBy(typeVariables + retTypeVariable)
              val serializableInputOutputFunctionResult =
                  ClassName.bestGuess(
                          "pl.andrzejressel.deeplambdaserialization.lib.SerializableInputOutputFunction${i}")
                      .parameterizedBy(typeVariables + retTypeVariable)

              val serializator =
                  ClassName.bestGuess("pl.andrzejressel.dto.serializator.Serializator")
              val kotlinSerializator =
                  ClassName.bestGuess(
                      "pl.andrzejressel.deeplambdaserialization.libkotlin.KotlinSerializator")

              val executeFunction =
                  FunSpec.builder("execute")
                      .addModifiers(KModifier.OVERRIDE)
                      .returns(retTypeVariable)
                      .addParameters(
                          alphabet.take(i).map {
                            ParameterSpec(it.lowercase(), TypeVariableName(it.toString()))
                          })
                      .addStatement("return f(${alphabet.take(i).joinToString { it.lowercase() }})")
                      .build()

              val functionInputSerializatorFunction =
                  (0 until i).map { index ->
                    val letter = alphabet.toList()[index]

                    FunSpec.builder("getArg${index + 1}Serializator")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(serializator.parameterizedBy(TypeVariableName(letter.uppercase())))
                        .addStatement("return %T.create()", kotlinSerializator)
                        .build()
                  }

              val getOutputSerializatorFunction =
                  FunSpec.builder("getOutputSerializator")
                      .addModifiers(KModifier.OVERRIDE)
                      .returns(serializator.parameterizedBy(retTypeVariable))
                      .addStatement("return %T.create()", kotlinSerializator)
                      .build()

              val serializableFunction =
                  TypeSpec.anonymousClassBuilder()
                      .superclass(serializableFunctionResult)
                      .addFunction(executeFunction)
                      .build()

              val serializableInputFunction =
                  TypeSpec.anonymousClassBuilder()
                      .superclass(serializableInputFunctionResult)
                      .addFunction(executeFunction)
                      .addFunctions(functionInputSerializatorFunction)
                      .build()

              val serializableInputOutputFunction =
                  TypeSpec.anonymousClassBuilder()
                      .superclass(serializableInputOutputFunctionResult)
                      .addFunction(executeFunction)
                      .addFunctions(functionInputSerializatorFunction)
                      .addFunction(getOutputSerializatorFunction)
                      .build()

              listOf(
                  FunSpec.builder("create")
                      .addModifiers(KModifier.INLINE)
                      .addParameter(
                          ParameterSpec.builder(
                                  "f",
                                  LambdaTypeName.get(
                                      parameters = typeVariables.map { ParameterSpec.unnamed(it) },
                                      returnType = retTypeVariable))
                              .addModifiers(KModifier.CROSSINLINE)
                              .build())
                      .addTypeVariables(typeVariables)
                      .addTypeVariable(retTypeVariable)
                      .returns(serializableFunctionResult)
                      .addStatement("return %L", serializableFunction)
                      .build(),
                  FunSpec.builder("createInput")
                      .addModifiers(KModifier.INLINE)
                      .addParameter(
                          ParameterSpec.builder(
                                  "f",
                                  LambdaTypeName.get(
                                      parameters = typeVariables.map { ParameterSpec.unnamed(it) },
                                      returnType = retTypeVariable))
                              .addModifiers(KModifier.CROSSINLINE)
                              .build())
                      .addTypeVariables(typeVariables.map { it.copy(reified = true) })
                      .addTypeVariable(retTypeVariable)
                      .returns(serializableInputFunctionResult)
                      .addStatement("return %L", serializableInputFunction)
                      .build(),
                  FunSpec.builder("createInputOutput")
                      .addModifiers(KModifier.INLINE)
                      .addParameter(
                          ParameterSpec.builder(
                                  "f",
                                  LambdaTypeName.get(
                                      parameters = typeVariables.map { ParameterSpec.unnamed(it) },
                                      returnType = retTypeVariable))
                              .addModifiers(KModifier.CROSSINLINE)
                              .build())
                      .addTypeVariables(typeVariables.map { it.copy(reified = true) })
                      .addTypeVariable(retTypeVariable.copy(reified = true))
                      .returns(serializableInputOutputFunctionResult)
                      .addStatement("return %L", serializableInputOutputFunction)
                      .build(),
              )
            }

        val file =
            FileSpec.builder(
                    "pl.andrzejressel.deeplambdadeserialization.libkotlin", "SerializableFunction")
                .also { fspec -> functions.forEach { fspec.addFunction(it) } }
                .build()

        file.writeTo(dir)
      }
    }

sourceSets { main { kotlin { srcDirs(generateSerializableFunction) } } }

tasks.jacocoTestReport {
  dependsOn("test")

  executionData.setFrom(fileTree(layout.buildDirectory).include("/jacoco/*.exec"))
  reports {
    xml.required = true
    html.required = true
  }
}

tasks.named("check") { dependsOn("jacocoTestReport") }
