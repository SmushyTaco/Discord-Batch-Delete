rootProject.name = settings.extra["project_name"] as String
pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm").version(settings.extra["kotlin_version"] as String)
        id("org.jetbrains.compose").version(settings.extra["compose_version"] as String)
        id("org.jetbrains.kotlin.plugin.compose").version(settings.extra["kotlin_version"] as String)
    }
}

