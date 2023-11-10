plugins {
  kotlin("jvm")
  alias(libs.plugins.maven.publish)
}

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

dependencies {
  implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
  compileOnly("pl.andrzejressel.deeplambdaserialization:entrypoint:$mvnVersion")
}

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
