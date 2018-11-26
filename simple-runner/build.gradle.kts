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
    implementation(project(":core"))
    compile(project(":connector-vk"))
    compile(project(":connector-tg"))
    implementation("org.litote.kmongo:kmongo-coroutine:3.9.0")
    implementation("org.mongodb:mongodb-driver-async:3.8.2")
    implementation("org.mongodb:mongo-java-driver:3.8.2")
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
            artifactId = "simple-runner"

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
        name = "simple-runner"
        desc = "Simple runtime to run single bot"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/kotlin-bot/yabot-framework.git"

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = "${project.version}"

            released = Date().toString()
            vcsTag = "${project.version}"
        })
    })
}
