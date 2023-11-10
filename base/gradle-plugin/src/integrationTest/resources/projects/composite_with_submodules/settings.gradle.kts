includeBuild("lib") {
  dependencySubstitution { substitute(module("aressel:lib")).using(project(":submodule")) }
}

includeBuild("app")
