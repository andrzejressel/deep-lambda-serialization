import pl.andrzejressel.deeplambdaserialization.buildplugin.ChildPlugin.Companion.License
import pl.andrzejressel.deeplambdaserialization.buildplugin.ChildPlugin.Companion.childSetup

plugins {
  `java-library`
  alias(libs.plugins.spotless)
  alias(libs.plugins.maven.publish)
}

childSetup(License.GPL)

repositories { mavenCentral() }

val mvnGroupId = parent!!.group.toString()
val mvnArtifactId = name
val mvnVersion = parent!!.version.toString()

ext { set("LICENCE", "LGPL") }

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
