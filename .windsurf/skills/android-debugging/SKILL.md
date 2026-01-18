---
name: android-debugging
description: Debug Android issues using logcat, stack traces, and device-specific tools
---

# Android Debugging

## When to Use This Skill

- When Android-specific crashes occur
- For ANR (Application Not Responding) issues
- When performance problems arise
- For device-specific bugs

## Phase 1: Logcat Analysis

### 1.1 Clear and Filter Logcat

```bash
# Clear existing logs
adb logcat -c

# Filter for your app only
adb logcat -s LongboiLauncher:* *:E

# Save logs to file
adb logcat > logs.txt

# Real-time monitoring
adb logcat | grep "LongboiLauncher"
```

### 1.2 Search for Critical Patterns

```bash
# Fatal exceptions
grep -r "FATAL EXCEPTION" logs.txt

# ANR traces
grep -r "ANR in" logs.txt

# Memory issues
grep -r "OutOfMemoryError" logs.txt

# Network issues
grep -r "NetworkOnMainThreadException" logs.txt

# Permission issues
grep -r "Permission denied" logs.txt
```

### 1.3 Decode Stack Traces

```bash
# Use retrace to deobfuscate stack traces
~/Android/Sdk/tools/proguard/bin/retrace.bat mapping.txt stacktrace.txt

# For R8 (modern Android)
./gradlew assembleRelease
# Find mapping file: app/build/outputs/mapping/release/mapping.txt
```

## Phase 2: Common Android Issues

### 2.1 ANR (Application Not Responding)

```bash
# Pull ANR traces from device
adb pull /data/anr/traces.txt

# Look for patterns:
# - "main" thread blocked
# - "Binder" transactions
# - Long-running operations
```

**Common Causes:**

- Database operations on main thread
- Network calls on main thread
- Heavy computation in UI callbacks
- Deadlocks

**Solutions:**

```kotlin
// BAD: Main thread operation
fun loadData() {
    val data = database.query() // Blocks main thread
    updateUI(data)
}

// GOOD: Background operation
fun loadData() = viewModelScope.launch {
    val data = withContext(Dispatchers.IO) {
        database.query()
    }
    withContext(Dispatchers.Main) {
        updateUI(data)
    }
}
```

### 2.2 Memory Leaks

```bash
# Use LeakCanary to detect
# Add to build.gradle:
debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'

# Monitor memory usage
adb shell dumpsys meminfo com.longboilauncher.app
```

**Common Leak Sources:**

- Static references to Activity/Context
- Inner classes holding outer class reference
- Unclosed resources (Cursor, Stream)
- Listeners not removed

### 2.3 Performance Issues

```bash
# Profile with Android Studio
# Tools > Profile

# Measure startup time
adb shell am start -W -n com.longboilauncher.app/.MainActivity

# Check frame drops
adb shell dumpsys gfxinfo com.longboilauncher.app
```

## Phase 3: Device-Specific Debugging

### 3.1 Check Android Version

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12+ specific code
} else {
    // Fallback for older versions
}
```

### 3.2 Verify Permissions

```bash
# Check granted permissions
adb shell dumpsys package com.longboilauncher.app | grep permission

# Test permission flow
adb shell pm grant com.longboilauncher.app android.permission.POST_NOTIFICATIONS
```

### 3.3 Screen Density Issues

```bash
# Test different densities
adb shell wm size 1080x1920  // Set resolution
adb shell wm density 420    // Set DPI
```

## Phase 4: Network and Connectivity

### 4.1 Debug Network Issues

```bash
# Use Charles Proxy or Wireshark
# Enable network debugging:
if (BuildConfig.DEBUG) {
    OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
}
```

### 4.2 Certificate Issues

```bash
# For HTTPS debugging in debug builds
// network_security_config.xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

## Phase 5: Compose-Specific Debugging

### 5.1 Recomposition Issues

```kotlin
// Add recomposition counts to composables
@Composable
fun MyComponent() {
    if (LocalInspectionMode.current) {
        LaunchedEffect(Unit) {
            Log.d("Compose", "MyComponent recomposed")
        }
    }
}
```

### 5.2 State Issues

```kotlin
// Debug state changes
val state by remember { mutableStateOf(initialValue) }
LaunchedEffect(state) {
    Log.d("State", "State changed to: $state")
}
```

## Debugging Tools

### Essential Commands

```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# View app logs
adb logcat -s LongboiLauncher:V

# Force crash for testing
adb shell am force-stop com.longboilauncher.app

# Clear app data
adb shell pm clear com.longboilauncher.app

# Take screenshot
adb shell screencap -p > screenshot.png

# Record screen
adb shell screenrecord /sdcard/demo.mp4
```

### Studio Tools

- Layout Inspector: UI debugging
- Memory Profiler: Memory analysis
- CPU Profiler: Performance profiling
- Network Inspector: Traffic analysis

## Integration

This skill works with:

- `systematic-debugging` for general debugging process
- `compose-performance` for Compose issues
- `mobile-security` for security-related bugs

## Output Format

Create bug report:

```
docs/bugs/YYYY-MM-DD-android-[issue].md
```

Include:

- Device information
- Android version
- Logcat output
- Stack trace
- Steps to reproduce
- Root cause analysis
