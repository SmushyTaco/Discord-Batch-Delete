import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.compose)
}
val projectName: Provider<String> = providers.gradleProperty("project_name")
val projectGroup: Provider<String> = providers.gradleProperty("project_group")
val projectVersion: Provider<String> = providers.gradleProperty("project_version")

val javaVersion: Provider<Int> = libs.versions.java.map { it.toInt() }

base.archivesName = projectName
group = projectGroup.get()
version = projectVersion.get()
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
dependencies {
    implementation(libs.coroutines)
    implementation(libs.materialDesktop)
    implementation(compose.components.resources)
    implementation(compose.desktop.currentOs)
}
java {
    toolchain {
        languageVersion = javaVersion.map { JavaLanguageVersion.of(it) }
        vendor = JvmVendorSpec.ADOPTIUM
    }
    sourceCompatibility = JavaVersion.toVersion(javaVersion.get())
    targetCompatibility = JavaVersion.toVersion(javaVersion.get())
}
val licenseFile = run {
    val rootLicense = layout.projectDirectory.file("LICENSE")
    val parentLicense = layout.projectDirectory.file("../LICENSE")
    when {
        rootLicense.asFile.exists() -> {
            logger.lifecycle("Using LICENSE from project root: {}", rootLicense.asFile)
            rootLicense
        }
        parentLicense.asFile.exists() -> {
            logger.lifecycle("Using LICENSE from parent directory: {}", parentLicense.asFile)
            parentLicense
        }
        else -> {
            logger.warn("No LICENSE file found in project or parent directory.")
            null
        }
    }
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.forkOptions.jvmArgs?.add("--enable-native-access=ALL-UNNAMED")
        sourceCompatibility = javaVersion.get().toString()
        targetCompatibility = javaVersion.get().toString()
        if (javaVersion.get() > 8) options.release = javaVersion
    }
    named<UpdateDaemonJvm>("updateDaemonJvm") {
        languageVersion = libs.versions.gradleJava.map { JavaLanguageVersion.of(it.toInt()) }
        vendor = JvmVendorSpec.ADOPTIUM
    }
    withType<JavaExec>().configureEach {
        defaultCharacterEncoding = "UTF-8"
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
    withType<Javadoc>().configureEach { options.encoding = "UTF-8" }
    withType<Test>().configureEach {
        defaultCharacterEncoding = "UTF-8"
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            extraWarnings = true
            jvmTarget = javaVersion.map { JvmTarget.valueOf("JVM_${if (it == 8) "1_8" else it}") }
        }
    }
    withType<Jar>().configureEach {
        licenseFile?.let {
            from(it) {
                rename { original -> "${original}_${archiveBaseName.get()}" }
            }
        }
    }
}
compose.desktop {
    application {
        mainClass = "MainKt"
        buildTypes.release.proguard.isEnabled = false
        jvmArgs.add("--enable-native-access=ALL-UNNAMED")
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "Discord Batch Delete"
            packageVersion = projectVersion.get()
            windows {
                iconFile = file("src/main/resources/icon.ico")
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