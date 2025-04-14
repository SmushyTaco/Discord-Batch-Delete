import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
group = project.extra["project_group"] as String
version = project.extra["project_version"] as String
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", project.extra["coroutines_version"] as String)
    implementation("net.java.dev.jna", "jna-platform", project.extra["java_native_access_version"] as String)
    implementation("org.jetbrains.compose.material3", "material3-desktop", project.extra["compose_version"] as String)
    implementation(compose.desktop.currentOs)
}
tasks {
    val javaVersion = JavaVersion.toVersion((project.extra["java_version"] as String).toInt())
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release = javaVersion.toString().toInt()
    }
    withType<JavaExec>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
    withType<Test>().configureEach { defaultCharacterEncoding = "UTF-8" }
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            extraWarnings = true
            jvmTarget = JvmTarget.valueOf("JVM_$javaVersion")
        }
    }
    withType<Jar>().configureEach {
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
            packageName = project.extra["project_name"] as String
            packageVersion = project.extra["project_version"] as String
            windows {
                menu = true
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "0D1ABBEE-B46E-4DF5-9C2F-851B6E3BB68A"
            }
            macOS {
                bundleID = "${project.extra["project_group"] as String}.${project.extra["project_name"] as String}"
            }
        }
    }
}