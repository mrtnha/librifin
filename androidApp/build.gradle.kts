import java.util.regex.Pattern
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    // Collects the licenses of everything bundled into the app (also the shared module's dependencies) at build
    // time, for the "Open Source Licenses" screen
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    // MainActivity is a FragmentActivity: Readium's EPUB renderer is a Fragment
    implementation(libs.androidx.fragment)
    // Required by Readium (its libraries are built with core library desugaring)
    coreLibraryDesugaring(libs.desugarJdkLibs)

    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "io.github.mrtnha.librifin"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.mrtnha.librifin"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

aboutLibraries {
    collect {
        // What the plugin can't see or only knows in part: the icons and fonts copied into the app,
        // desugar_jdk_libs, and exact license texts
        configPath = file("config")
    }
    library {
        // On Android, JetBrains' Compose and Lifecycle artifacts only point to Google's androidx ones, which are
        // listed already (same names, same license)
        exclusionPatterns.addAll(
            Pattern.compile("org\\.jetbrains\\.compose\\..*"),
            Pattern.compile("org\\.jetbrains\\.androidx\\..*"),
            // Replaced by entries in config/ with their exact license text: the plugin only has a generic one
            // without the copyright line (Readium's three modules become one entry)
            Pattern.compile("org\\.readium\\.kotlin-toolkit:.*"),
            Pattern.compile("org\\.jsoup:jsoup"),
            Pattern.compile("org\\.slf4j:slf4j-api"),
        )
    }
}
