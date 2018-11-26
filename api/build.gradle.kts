import com.jfrog.bintray.gradle.BintrayExtension
import java.util.Date

apply {
    plugin("org.jetbrains.kotlin.jvm")
}

plugins {

}

kotlin {

}



dependencies {
    implementation("name.alatushkin.utils:common-http-client:0.1")
    compile("com.fasterxml.jackson.core:jackson-annotations:2.9.7")
    implementation("com.vdurmont:emoji-java:4.0.0")
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
            artifactId = "api"

        }
    }
}

bintray {
    user = project.properties["bintrayUser"] as? String ?: System.getenv("BINTRAY_USER")
    key = project.properties["bintrayKey"] as? String ?: System.getenv("BINTRAY_KEY")
    setPublications("mavenJava")
    publish = true
    override = true

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "snapshot"
        userOrg = "kotlin-bot"
        name = "api"
        desc = "Common bot api"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/kotlin-bot/yabot-framework.git"

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = "${project.version}"

            released = Date().toString()
            vcsTag = "${project.version}"
        })
    })
}
