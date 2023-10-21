includeBuild("lib") {
  dependencySubstitution { substitute(module("aressel:lib")).using(project(":")) }
}

includeBuild("app")
