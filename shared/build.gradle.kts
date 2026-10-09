import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
       namespace = "io.github.mrtnha.librifin.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       // For the reader's bundled fonts in src/androidMain/assets; without it, assets aren't packaged.
       androidResources {
           enable = true
       }
       withHostTest {}
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            // Showing and hiding the system bars in the reader (already in the app via Compose and Readium)
            implementation(libs.androidx.core.ktx)
            // EPUB parsing, and the EPUB renderer (a Fragment, shown in Compose via fragment-compose)
            implementation(libs.readium.streamer)
            implementation(libs.readium.navigator)
            implementation(libs.androidx.fragment.compose)
            // Starting the reader's WebView early (already in the app via Readium)
            implementation(libs.androidx.webkit)
        }
        commonMain.dependencies {
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
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            // Reads the license list the app generates at build time (Open Source Licenses screen)
            implementation(libs.aboutlibraries.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}