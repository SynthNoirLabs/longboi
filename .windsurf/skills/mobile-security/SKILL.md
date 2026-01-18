---
name: mobile-security
description: Review Android code for security vulnerabilities and ensure mobile security best practices
---

# Mobile Security Review

## When to Use This Skill

- Before releasing to production
- During code reviews
- When handling sensitive data
- For security audits

## Security Checklist

### 1. Data Protection

#### ✅ Required Practices

```kotlin
// Use BuildConfig for sensitive values
class ApiConfig {
    private val apiKey: String
        get() = if (BuildConfig.DEBUG) {
            "debug_key"
        } else {
            BuildConfig.API_KEY // From build.gradle or keystore
        }
}

// Encrypt sensitive data at rest
@Composable
fun SecureStorage() {
    val context = LocalContext.current
    val encryptedPrefs = remember {
        EncryptedSharedPreferences.create(
            "secure_prefs",
            "master_key_alias",
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        )
    }
}
```

#### ❌ Forbidden Practices

```kotlin
// BAD: Hardcoded secrets
class BadExample {
    private val API_KEY = "sk-1234567890abcdef" // NEVER do this

    // BAD: Secrets in strings.xml
    // <string name="api_key">sk-1234567890abcdef</string>
}
```

### 2. Network Security

#### HTTPS Enforcement

```xml
<!-- network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.longboilauncher.com</domain>
    </domain-config>

    <!-- Debug builds can allow HTTP for testing -->
    <debug-overrides>
        <domain-config cleartextTrafficPermitted="true">
            <domain includeSubdomains="true">10.0.2.2</domain> // Emulator
        </domain-config>
    </debug-overrides>
</network-security-config>
```

#### Certificate Pinning

```kotlin
// Certificate pinning for sensitive APIs
val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(
        CertificatePinner.Builder()
            .add("api.longboilauncher.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()
    )
    .build()
```

### 3. Permissions

#### Runtime Permission Checks

```kotlin
@Composable
fun PermissionAwareFeature() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    if (permissionState.status.isGranted) {
        // Feature implementation
    } else {
        // Explain why permission is needed
        PermissionRationale(
            onRequestPermission = { permissionState.launchPermissionRequest() }
        )
    }
}
```

#### Minimal Permission Set

```xml
<!-- AndroidManifest.xml - Only request what's necessary -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<!-- Don't add permissions you don't use -->
```

### 4. Code Security

#### ProGuard/R8 Configuration

```proguard
# proguard-rules.pro
# Keep but obfuscate
-keep class com.longboilauncher.app.data.model.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Prevent reflection attacks
-keepattributes Signature
-keepattributes *Annotation*
```

#### Root Detection (Optional)

```kotlin
class SecurityUtils {
    fun isRooted(): Boolean {
        return checkSuBinary() || checkRootkitApps()
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su"
        )
        return paths.any { File(it).exists() }
    }
}
```

### 5. WebView Security

```kotlin
@Composable
fun SecureWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false // Disable JS if not needed
                settings.domStorageEnabled = false
                settings.databaseEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false

                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        },
        modifier = modifier
    )
}
```

## Security Review Process

### 1. Code Analysis

```bash
# Search for potential security issues
grep -r "API_KEY\|SECRET\|PASSWORD" --include="*.kt" --include="*.java" .
grep -r "http://" --include="*.kt" --include="*.xml" .
grep -r "Log\." --include="*.kt" --include="*.java" .
```

### 2. Dependency Security

```bash
# Check for vulnerable dependencies
./gradlew dependencyCheckAnalyze

# Or use OWASP Dependency-Check
plugins {
    id("org.owasp.dependencycheck") version "9.0.7"
}
```

### 3. Manifest Security

```xml
<!-- AndroidManifest.xml security checks -->
<application
    android:allowBackup="false"          // Prevent backup of app data
    android:debuggable="false"           // Must be false in production
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false">

    <!-- Exported components must be protected -->
    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:permission="com.longboilauncher.permission.LAUNCH">
    </activity>
</application>
```

## Common Security Issues & Fixes

### 1. Hardcoded Secrets

```kotlin
// BEFORE
private val API_KEY = "secret_key_here"

// AFTER
private val API_KEY: String
    get() = BuildConfig.API_KEY
```

### 2. Insecure Storage

```kotlin
// BEFORE
SharedPreferences.Editor(context)
    .putString("password", password)
    .apply()

// AFTER
EncryptedSharedPreferences.create(...)
```

### 3. Unencrypted Network Traffic

```xml
<!-- BEFORE -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- AFTER -->
<uses-permission android:name="android.permission.INTERNET" />
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false">
```

## Security Testing

### 1. Static Analysis

```bash
# Use Android Studio's "Analyze > Inspect Code"
# Or use SonarQube for continuous analysis
```

### 2. Dynamic Testing

```bash
# Use MobSF (Mobile Security Framework)
# Or OWASP ZAP for API testing
```

### 3. Penetration Testing

- Test for common vulnerabilities
- Verify data encryption
- Check permission abuse
- Test API security

## Security Best Practices Summary

### Do's ✅

- Use BuildConfig for secrets
- Enforce HTTPS
- Encrypt sensitive data
- Minimize permissions
- Obfuscate release builds
- Use certificate pinning for sensitive APIs
- Validate all inputs
- Implement proper session management

### Don'ts ❌

- Hardcode secrets
- Use HTTP for sensitive data
- Store sensitive data in plain text
- Request unnecessary permissions
- Ship with debuggable=true
- Ignore SSL certificate errors
- Trust user input blindly
- Log sensitive information

## Integration

This skill works with:

- `executing-plans` before release
- `android-debugging` for security-related bugs
- `systematic-debugging` for security issues

## Output Format

Security report:

```
docs/security/YYYY-MM-DD-security-review.md
```

Include:

- Security checklist results
- Vulnerabilities found
- Fixes implemented
- Risk assessment
- Recommendations
