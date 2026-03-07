# Benchmark specific rules
-keep class com.longboilauncher.benchmark.** { *; }
-dontwarn com.longboilauncher.benchmark.**

# Suppress warnings from common libraries that R8 can't find in the test classpath
-dontwarn androidx.annotation.**
-dontwarn androidx.arch.core.**
-dontwarn androidx.lifecycle.**
-dontwarn androidx.profileinstaller.**
-dontwarn androidx.startup.**
-dontwarn androidx.tracing.**
-dontwarn androidx.test.**

# Disable obfuscation and shrinking for the benchmark project itself
-dontobfuscate
-dontshrink
-keepattributes *Annotation*,SourceFile,LineNumberTable
