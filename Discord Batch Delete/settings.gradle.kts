val projectName: String by System.getProperties()
rootProject.name = projectName
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        val composeVersion: String by System.getProperties()
        id("org.jetbrains.compose").version(composeVersion)
    }
}

