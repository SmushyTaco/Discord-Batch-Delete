import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    val composeVersion: String by System.getProperties()
    id("org.jetbrains.compose").version(composeVersion)
}
val projectGroup: String by project
group = projectGroup
val projectVersion: String by project
version = projectVersion
repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
dependencies {
    val javaNativeAccessVersion: String by project
    implementation("net.java.dev.jna", "jna-platform", javaNativeAccessVersion)
    val coroutinesVersion: String by project
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutinesVersion)
    implementation(compose.desktop.currentOs)
}
tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            val javaVersion: String by project
            jvmTarget = javaVersion
        }
    }
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest { attributes["Main-Class"] = "MainKt" }
        configurations["compileClasspath"].forEach { from(zipTree(it.absoluteFile)) }
    }
}
compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            val projectName: String by System.getProperties()
            packageName = projectName
            val projectVersion: String by project
            packageVersion = projectVersion
            windows {
                menu = true
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "0D1ABBEE-B46E-4DF5-9C2F-851B6E3BB68A"
            }
            macOS {
                val projectGroup: String by project
                bundleID = "$projectGroup.$projectName"
            }
        }
    }
}