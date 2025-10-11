import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
val projectGroup = providers.gradleProperty("project_group")
val projectVersion = providers.gradleProperty("project_version")
val coroutinesVersion = providers.gradleProperty("coroutines_version")
val javaNativeAccessVersion = providers.gradleProperty("java_native_access_version")
val composeVersion = providers.gradleProperty("compose_version")
val javaVersion = providers.gradleProperty("java_version")
val projectName = providers.gradleProperty("project_name")
group = projectGroup.get()
version = projectVersion.get()
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion.get()}")
    implementation("net.java.dev.jna:jna-platform:${javaNativeAccessVersion.get()}")
    implementation("org.jetbrains.compose.material3:material3-desktop:${composeVersion.get()}-beta06")
    implementation(compose.desktop.currentOs)
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.get()
        targetCompatibility = javaVersion.get()
        options.release = javaVersion.get().toInt()
    }
    withType<JavaExec>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
    withType<Test>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            extraWarnings = true
            jvmTarget = JvmTarget.valueOf("JVM_${javaVersion.get()}")
        }
    }
}
compose.desktop {
    application {
        mainClass = "MainKt"
        buildTypes.release.proguard.isEnabled = false
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = projectName.get()
            packageVersion = projectVersion.get()
            windows {
                menu = true
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "0D1ABBEE-B46E-4DF5-9C2F-851B6E3BB68A"
            }
            macOS {
                bundleID = "${projectGroup.get()}.${projectName.get()}"
            }
        }
    }
}