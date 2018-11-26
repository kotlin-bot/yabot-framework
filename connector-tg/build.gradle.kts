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
    implementation("com.github.pengrad:java-telegram-bot-api:4.1.0")
    implementation(project(":api"))
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
            artifactId = "connector-tg"

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
        name = "connector-tg"
        desc = "Connecto your bot to Telegram"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/kotlin-bot/yabot-framework.git"

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = "${project.version}"

            released = Date().toString()
            vcsTag = "${project.version}"
        })
    })
}
