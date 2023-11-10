dependencyResolutionManagement {
  versionCatalogs { create("libs") { from(files("../../gradle/libs.versions.toml")) } }
}

includeBuild("../../base") {
  dependencySubstitution {
    substitute(module("pl.andrzejressel.deeplambdaserialization:gradle-plugin"))
        .using(project(":gradle-plugin"))
    substitute(module("pl.andrzejressel.deeplambdaserialization:lib")).using(project(":lib"))
    substitute(module("pl.andrzejressel.deeplambdaserialization:lib-kotlin"))
        .using(project(":lib-kotlin"))
    substitute(module("pl.andrzejressel.deeplambdaserialization:entrypoint"))
        .using(project(":entrypoint"))
  }
}

includeBuild("../library") {
  dependencySubstitution {
    substitute(module("pl.andrzejressel.deeplambdaserialization.aws:gradle-plugin"))
        .using(project(":gradle-plugin"))
    substitute(module("pl.andrzejressel.deeplambdaserialization.aws:lib")).using(project(":lib"))
    substitute(module("pl.andrzejressel.deeplambdaserialization.aws:handler"))
        .using(project(":handler"))
  }
}

rootProject.name = "integration-test"

include("example", "lambdas")
