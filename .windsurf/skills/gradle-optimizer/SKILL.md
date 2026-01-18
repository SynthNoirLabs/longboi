---
name: gradle-optimizer
description: Optimize Gradle build speed and configuration for Longboi Launcher
---

# Gradle Optimization

## When to Use This Skill

- When builds are slow (> 2 minutes)
- Before adding new modules
- When CI builds are timing out
- For general performance improvement

## Build Speed Optimizations

### 1. gradle.properties Configuration

```properties
# JVM Settings
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC

# Parallel execution
org.gradle.parallel=true

# Build caching
org.gradle.caching=true
org.gradle.buildcache=true

# Configuration on demand
org.gradle.configureondemand=true

# Daemon settings
org.gradle.daemon=true
org.gradle.daemon.idletimeout=3600000

# Android specific
android.useAndroidX=true
android.enableJetifier=true
android.enableBuildCache=true
android.enableR8.fullMode=true
```

### 2. Version Catalog Setup

#### settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            // Kotlin
            version("kotlin", "2.0.21")
            version("kotlinx-coroutines", "1.7.3")

            // Android
            version("android-gradle-plugin", "8.7.3")
            version("androidx-compose-bom", "2025.12.00")

            // Libraries
            library("androidx-compose-bom", "androidx.compose", "compose-bom").versionRef("androidx-compose-bom")
            library("androidx-compose-ui", "androidx.compose.ui", "ui")
            library("androidx-compose-ui-tooling", "androidx.compose.ui", "ui-tooling")
            library("androidx-compose-material3", "androidx.compose.material3", "material3")

            // Hilt
            version("hilt", "2.52")
            library("hilt-android", "com.google.dagger", "hilt-android").versionRef("hilt")
            library("hilt-compiler", "com.google.dagger", "hilt-android-compiler").versionRef("hilt")

            // Testing
            library("junit", "junit", "junit", "4.13.2")
            library("androidx-test-ext", "androidx.test.ext", "junit", "1.1.5")

            // Plugins
            plugin("android-application", "com.android.application").versionRef("android-gradle-plugin")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").versionRef("kotlin")
            plugin("hilt-android", "com.google.dagger.hilt.android").versionRef("hilt")
        }
    }
}
```

### 3. Module-Level build.gradle.kts

#### app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
}

android {
    namespace = "com.longboilauncher.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.longboilauncher.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Disable unused build steps in debug
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Enable R8 full mode
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"

        // Enable compose compiler metrics
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir}/compose_compiler"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Core Android
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Modules
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":feature:home"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
}
```

## Module Structure Optimization

### 1. Keep Modules Small

```kotlin
// Good module structure
:core:designsystem    // UI components
:core:common         // Utilities
:core:data          // Repository layer
:core:network       // API layer
:feature:home       // Home feature
:feature:settings   // Settings feature
```

### 2. Avoid Circular Dependencies

```kotlin
// Use dependency inversion
:feature:home -> :core:data -> :core:network
// NOT
:core:network -> :feature:home
```

### 3. Use api vs Implementation Correctly

```kotlin
dependencies {
    // api: Exposed to consumers
    api(libs.androidx.compose.ui)

    // implementation: Internal only
    implementation(libs.hilt.android)
}
```

## Build Performance Monitoring

### 1. Build Scan

```bash
# Enable build scans
./gradlew assembleDebug --scan

# Analyze build performance
# Visit https://scans.gradle.com
```

### 2. Profile Builds

```bash
# Profile specific task
./gradlew assembleDebug --profile

# View report: build/reports/profile/profile-*.html
```

### 3. Dry Run

```bash
# Check task dependencies
./gradlew assembleDebug --dry-run
```

## Common Issues & Solutions

### 1. Slow Compilation

```kotlin
// Enable incremental compilation
kotlin {
    incrementalJava = true
}

// Use KSP instead of kapt where possible
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}
```

### 2. Memory Issues

```properties
# Increase heap size
org.gradle.jvmargs=-Xmx6144m -XX:MaxMetaspaceSize=512m

# Enable parallel GC
org.gradle.jvmargs=-Xmx4096m -XX:+UseG1GC
```

### 3. Network Dependency Resolution

```properties
# Use offline mode when possible
org.gradle.offline=false

# Configure custom repositories if needed
systemProp.https.proxyHost=proxy.company.com
systemProp.https.proxyPort=8080
```

## CI/CD Optimization

### 1. Gradle Wrapper

```bash
# Always use wrapper
./gradlew wrapper --gradle-version=8.11.1

# Commit wrapper files
git add gradle/wrapper gradlew
```

### 2. Build Cache

```yaml
# GitHub Actions example
- name: Setup Gradle
  uses: gradle/gradle-build-action@v2
  with:
    cache-read-only: true
    gradle-home-cache-cleanup: true
```

### 3. Parallel Tasks

```bash
# Run tasks in parallel
./gradlew testDebugUnitTest assembleDebug --parallel
```

## Optimization Checklist

### Before Changes

- [ ] Current build time baseline
- [ ] Module dependencies mapped
- [ ] Identify bottlenecks

### After Changes

- [ ] Build time improved by > 20%
- [ ] All tests pass
- [ ] No circular dependencies
- [ ] Cache hit rate > 70%

## Integration

This skill works with:

- `executing-plans` when adding new modules
- `android-debugging` for build issues
- `systematic-debugging` for build failures

## Success Metrics

- **Debug build**: < 30 seconds
- **Release build**: < 2 minutes
- **Incremental build**: < 5 seconds
- **Cache hit rate**: > 70%
- **Parallel efficiency**: > 80%

## Output Format

Optimization report:

```
docs/build/YYYY-MM-DD-gradle-optimization.md
```

Include:

- Before/after build times
- Changes made
- Performance improvements
- Remaining bottlenecks
