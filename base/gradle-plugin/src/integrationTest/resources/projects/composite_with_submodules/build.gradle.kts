tasks.register("clean") {
  dependsOn(gradle.includedBuild("app").task(":submodule:clean"))
  dependsOn(gradle.includedBuild("lib").task(":submodule:clean"))
}

tasks.register("run") { dependsOn(gradle.includedBuild("app").task(":submodule:run")) }
