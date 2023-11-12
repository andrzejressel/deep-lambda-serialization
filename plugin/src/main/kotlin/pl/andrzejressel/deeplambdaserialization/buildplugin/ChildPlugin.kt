package pl.andrzejressel.deeplambdaserialization.buildplugin

import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class ChildPlugin : Plugin<Project> {

  companion object {

    enum class License(val spdxId: String) {
      GPL("GPL-2.0-or-later"),
      LGPL("LGPL-3.0-or-later")
    }

    @JvmStatic
    fun childSetupGPL(p: Any) {
      (p as Project).childSetup(License.GPL)
    }

    fun Project.childSetup(license: License) {
      apply<ChildPlugin>()
      configure<SpotlessExtension> {
        kotlin {
          target("src/**/*.kt")
          ktfmt()
          licenseHeader("// SPDX-License-Identifier: ${license.spdxId}")
          endWithNewline()
        }
        groovyGradle {
          target("*.gradle") // default target of groovyGradle
          greclipse()
        }
        kotlinGradle {
          target("*.gradle.kts", "src/**/*.gradle.kts") // default target for kotlinGradle
          ktfmt() // or ktfmt() or prettier()
          endWithNewline()
        }
        java {
          importOrder()
          removeUnusedImports()
          cleanthat()
          palantirJavaFormat("2.38.0").style("GOOGLE")
          target("src/**/*.java")
          licenseHeader("// SPDX-License-Identifier: ${license.spdxId}")
          endWithNewline()
        }
      }
      val mvnGroupId = parent!!.group.toString()
      val mvnArtifactId = name
      val mvnVersion = parent!!.version.toString()

      version = mvnVersion

      configure<MavenPublishBaseExtension> {
        coordinates(mvnGroupId, mvnArtifactId, mvnVersion)

        pom {
          licenses {
            when (license) {
              License.GPL ->
                  license {
                    name = "The GNU General Public License v2.0"
                    url = "https://www.gnu.org/licenses/gpl-2.0.txt"
                    distribution = "https://www.gnu.org/licenses/gpl-2.0.txt"
                  }
              License.LGPL ->
                  license {
                    name = "Gnu Lesser General Public License"
                    url = "http://www.gnu.org/licenses/lgpl.txt"
                    distribution = "http://www.gnu.org/licenses/lgpl.txt"
                  }
            }
          }
        }
      }
    }
  }

  override fun apply(target: Project) {}
}
