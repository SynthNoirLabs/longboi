# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep launcher models
-keep class com.longboilauncher.app.data.model.** { *; }

# Readable crash reports
-keepattributes SourceFile,LineNumberTable

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.longboilauncher.app.**$$serializer { *; }
-keepclassmembers class com.longboilauncher.app.** {
    *** Companion;
}

# Coil custom components
-keep class com.longboilauncher.app.core.icons.AppEntryFetcher$Factory { *; }
-keep class com.longboilauncher.app.core.icons.AppEntryKeyer { *; }
