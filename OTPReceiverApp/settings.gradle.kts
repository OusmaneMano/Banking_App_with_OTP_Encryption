pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // This is what triggers the warning
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OTPReceiverApp"
include(":app")