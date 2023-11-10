tasks.register("clean") {
  dependsOn(gradle.includedBuild("app").task(":clean"))
  dependsOn(gradle.includedBuild("lib").task(":clean"))
}

tasks.register("run") { dependsOn(gradle.includedBuild("app").task(":run")) }
