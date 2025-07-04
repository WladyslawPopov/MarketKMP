import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqlDelight)
}

sqldelight {
    databases {
        create("MarketDB") {
            packageName.set("market.engine.shared")
            schemaOutputDirectory.set(file("src/main/sqldelight"))
            migrationOutputDirectory.set(file("src/main/sqldelight/migrations"))
            version = 12
            verifyMigrations.set(true)
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.zoomable)
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.datastore)

            api(libs.koin.core)
            implementation(libs.koin.coroutines)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)

            implementation(libs.paging.compose.common)

            implementation(libs.coil.svg)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.decompose)
            implementation(libs.decompose.extensions)

            implementation(libs.richeditor.compose)

            // Enables FileKit without Compose dependencies
            implementation(libs.filekit.core)

            // Enables FileKit with Composable utilities
            implementation(libs.filekit.compose)

            implementation(libs.slf4j.simple)

            implementation(libs.skiko)

            implementation(libs.reorderable)

            implementation(libs.ksoup.html)
            implementation(libs.ksoup.entities)
        }
        val jvmMain by getting
        jvmMain.dependencies {
            implementation(libs.sqlite.driver)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.java)
            implementation(libs.androidx.runtime.desktop)
        }
        iosMain.dependencies {
            implementation(libs.native.driver)
            implementation(libs.ktor.client.darwin)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
        }
        nativeMain.dependencies {
            implementation(libs.native.driver)
            implementation(libs.ktor.client.darwin)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
        }
        androidMain.dependencies {
            implementation(libs.android.driver)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.system.ui.controller)

            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.accompanist.permissions)
            implementation(libs.accompanist.swiperefresh)
        }
    }
}

android {
    namespace = "market.engine.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
