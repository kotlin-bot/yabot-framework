import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.bundling.Jar
import java.util.Date


plugins {

}

kotlin {

}

dependencies {
    implementation(project(":core"))
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

publishing {

    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
        }
    }
    publications {
        create("mavenJava", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar)
            artifactId = "tests"

        }
    }
}

bintray {
    user = project.properties["bintrayUser"]?.toString() ?: System.getenv("BINTRAY_USER")
    key = project.properties["bintrayKey"]?.toString() ?: System.getenv("BINTRAY_KEY")
    setPublications("mavenJava")
    publish = true
    override = true

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "snapshot"
        userOrg = "kotlin-bot"
        name = "tests"
        desc = "Library to write tests for your bots"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/kotlin-bot/yabot-framework.git"

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = "${project.version}"

            released = Date().toString()
            vcsTag = "${project.version}"
        })
    })
}
