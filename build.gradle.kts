import org.jetbrains.kotlin.gradle.dsl.Coroutines

import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.3.0" apply false
    java
    id("com.gradle.build-scan") version "1.16"
    id("com.jfrog.bintray") version "1.8.1" apply false
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven("https://dl.bintray.com/kotlin-bot/snapshot")
        maven("https://dl.bintray.com/alatushkin/maven")
    }

    version = "0.1"
    group = "org.kotlin-bot"
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.jfrog.bintray")
        plugin("org.gradle.maven-publish")
    }

    tasks.withType(KotlinCompile::class.java).all {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}

subprojects {
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:0.22.5")
    }
}
