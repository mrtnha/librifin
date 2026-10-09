import java.util.regex.Pattern
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    // Collects the licenses of everything bundled into the app at build time, for the "Open Source Licenses" screen
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigationevent.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    // Showing and hiding the system bars in the reader (already in the app via Compose and Readium)
    implementation(libs.androidx.core.ktx)
    // EPUB parsing, and the EPUB renderer (a Fragment, shown in Compose via fragment-compose)
    implementation(libs.readium.streamer)
    implementation(libs.readium.navigator)
    implementation(libs.androidx.fragment.compose)
    // MainActivity is a FragmentActivity: Readium's EPUB renderer is a Fragment
    implementation(libs.androidx.fragment)
    // Starting the reader's WebView early (already in the app via Readium)
    implementation(libs.androidx.webkit)
    // Reads the license list the app generates at build time (Open Source Licenses screen)
    implementation(libs.aboutlibraries.core)
    // Required by Readium (its libraries are built with core library desugaring)
    coreLibraryDesugaring(libs.desugarJdkLibs)

    constraints {
        // Material 3 asks for an older ripple: keep it on the same Compose release as the rest
        implementation(libs.compose.materialRipple)
    }

    // kotlin.test on JUnit 4, which the unit tests run on
    testImplementation(libs.kotlin.test.junit)
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
