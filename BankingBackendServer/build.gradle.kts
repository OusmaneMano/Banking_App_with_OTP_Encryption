// build.gradle.kts (Project level)
plugins {
    kotlin("jvm") version "1.9.10" // Optional, for Kotlin support if needed
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}