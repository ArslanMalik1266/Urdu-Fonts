import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // Generates WebsCarePlacements constants and the AdMob/UMP app ID in the merged manifest from Firebase
    alias(libs.plugins.webscare.placements)

}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val bearerToken = localProperties.getProperty("BEARER_TOKEN") ?: ""
val apiKey = localProperties.getProperty("API_KEY")
    ?: error("API_KEY missing from local.properties (the urdufonts.com API key; ask the backend team)")

val appVersionCode = 7
val appVersionName = "1.0.7"

base.archivesName.set("UrduFonts - V$appVersionCode($appVersionName)")
android {
    namespace = "com.urdufonts.app"
    compileSdk {
        // WebsCareAds 4.2 requires compiling against API 37
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.urdufonts.app"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "BEARER_TOKEN", "\"$bearerToken\"")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file("D:/UrduFonts/app/upload-keystore-urdufonts.jks")
            storePassword = project.findProperty("KEYSTORE_PASSWORD") as String
            keyPassword = project.findProperty("KEY_PASSWORD") as String
            keyAlias = project.findProperty("KEY_ALIAS") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // WebsCareAds 4.2: core + Next-Gen AdMob (Meta bids inside AdMob). Never add play-services-ads next to it.
    implementation(libs.webscare.ads)
    implementation(libs.webscare.ads.admob)
    implementation(libs.webscare.ads.admob.meta)
    implementation(libs.play.billing.ktx)

}