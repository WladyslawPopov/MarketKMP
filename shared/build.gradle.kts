import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")


    sourceSets {
        commonMain.dependencies {
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            api(libs.koin.core)
            implementation(libs.koin.compose)


            implementation(libs.coil.compose)

            implementation(libs.coil3.network)

            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
//            implementation(libs.realm.sync)
//            implementation(libs.realm.base)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
        }
//        val desktopMain by getting
//        desktopMain.dependencies {
//            implementation(compose.desktop.currentOs)
//            implementation(libs.kotlinx.coroutines.swing)
//            implementation(libs.ktor.client.java)
//            implementation(libs.androidx.runtime.desktop)
//        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)

            implementation(libs.ktor.client.android)

            implementation(libs.system.ui.controller)

            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.datastore)
            implementation(libs.accompanist.permissions)
            implementation(libs.coil3.svg)
            implementation(libs.coil3.gif)
            implementation(libs.coil3.video)
            implementation(libs.coil.compose.core)
        }
    }
}

android {
    namespace = "com.example.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

compose.desktop {
}

