dependencyResolutionManagement {
  versionCatalogs { create("libs") { from(files("../../gradle/libs.versions.toml")) } }
}

includeBuild("../../base") {
  dependencySubstitution {
    substitute(module("pl.andrzejressel.deeplambdaserialization:gradle-plugin"))
        .using(project(":gradle-plugin"))
    substitute(module("pl.andrzejressel.deeplambdaserialization:lib")).using(project(":lib"))
    substitute(module("pl.andrzejressel.deeplambdaserialization:entrypoint"))
        .using(project(":entrypoint"))
  }
}

rootProject.name = "deep-lambda-serialization-aws"

includeBuild("../../plugin")

include("handler", "lib", "gradle-plugin")
