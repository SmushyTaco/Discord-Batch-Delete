val projectName: Provider<String> = providers.gradleProperty("project_name")
rootProject.name = projectName.get()
pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    val foojayResolverVersion = providers.gradleProperty("foojay_resolver_version")
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention").version(foojayResolverVersion.get())
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}