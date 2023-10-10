plugins {
    java
    alias(libs.plugins.spotless)
    id("com.vanniktech.maven.publish")
}

repositories {
    mavenCentral()
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