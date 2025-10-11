val projectName = providers.gradleProperty("project_name")
rootProject.name = projectName.get()
pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    val kotlinVersion = providers.gradleProperty("kotlin_version")
    val composeVersion = providers.gradleProperty("compose_version")
    plugins {
        kotlin("jvm").version(kotlinVersion.get())
        id("org.jetbrains.compose").version(composeVersion.get())
        id("org.jetbrains.kotlin.plugin.compose").version(kotlinVersion.get())
    }
}

