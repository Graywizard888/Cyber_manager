# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Apache Commons
-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
