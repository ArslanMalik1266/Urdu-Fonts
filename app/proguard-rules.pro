# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# ─── Retain Retrofit and Gson annotations/metadata ────────────────────────────
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8

# ─── Keep all Gson DTO models intact to prevent serialization bugs ────────────
-keep class com.urdufonts.app.data.remote.dto.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ─── Keep Retrofit API Interface classes ──────────────────────────────────────
-keep class com.urdufonts.app.data.remote.api.** { *; }
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# ─── Keep Domain Models ───────────────────────────────────────────────────────
-keep class com.urdufonts.app.domain.models.** { *; }

# ─── Keep Koin & ViewModels (ensures dependency injection doesn't crash) ──────
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Keep Room Database entities and DAOs ─────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep class com.urdufonts.app.data.local.dao.** { *; }
-keep class com.urdufonts.app.data.local.entity.** { *; }
-dontwarn androidx.room.paging.**

# ─── Google Mobile Ads SDK Proguard rules ──────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ─── WebsCareAds Library Keep Rules ───────────────────────────────────────────
-keep class com.webscare.ads.** { *; }
-keepclassmembers class com.webscare.ads.** { *; }

# ─── Facebook Shimmer ──────────────────────────────────────────────────────────
-keep class com.facebook.shimmer.** { *; }