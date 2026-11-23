plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    android {
        namespace = "com.urdufonts.shared"
        compileSdk = 37
        minSdk = 24
        // Packs the shared composeResources (images, fonts, strings) into the Android app
        androidResources { enable = true }
    }

    // iOS targets (devices + Apple-Silicon simulators): Kotlin/Native for iOS only builds on macOS; on Windows these are skipped.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    compilerOptions {
        // expect/actual classes are still Beta in Kotlin
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.addAll("kotlin.time.ExperimentalTime", "kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            api(libs.koin.core)
            api(libs.androidx.datastore.preferences.core)
            api(libs.androidx.room.runtime)
            implementation(libs.kotlinx.io.core)
            api(libs.kotlinx.datetime)

            // ── UI (Compose Multiplatform) ──
            api(libs.mp.compose.runtime)
            api(libs.mp.compose.foundation)
            api(libs.mp.compose.ui)
            api(libs.mp.compose.material3)
            api(libs.mp.compose.resources)
            api(libs.mp.compose.ui.tooling.preview)
            api(libs.mp.lifecycle.viewmodel.compose)
            api(libs.mp.lifecycle.runtime.compose)
            api(libs.mp.navigation.compose)
            api(libs.coil3.compose)
            implementation(libs.coil3.network.ktor)
            implementation(libs.coil3.svg)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

// Room's annotation processor runs once per platform and writes AppDatabaseConstructor's actuals
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

// Generated resource accessors: com.urdufonts.app.resources.Res.drawable.x / .font.x / .string.x
compose.resources {
    publicResClass = true
    packageOfResClass = "com.urdufonts.app.resources"
    generateResClass = always
}