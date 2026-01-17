# Longboi Launcher - Jetpack Compose

A minimalist Android launcher built with Jetpack Compose, inspired by Longboi Launcher's clean and efficient design.

## Features

- **Minimalist UI**: Clean, distraction-free app list
- **Fast Search**: Quickly find apps by name
- **Gesture Support**: Scroll to reveal search bar
- **Material You**: Dynamic color theming
- **Performance Optimized**: Efficient lazy loading and state management

## Architecture

Built with modern Android architecture patterns:

- **MVVM** with Hilt for dependency injection
- **Repository pattern** for app data management
- **Jetpack Compose** for UI
- **Kotlin Coroutines** for async operations

## Key References

This project was developed with inspiration and guidance from:

### Official Android Resources

- [Android Compose Samples](https://github.com/android/compose-samples) - Official samples demonstrating Compose best practices
- [Now in Android](https://github.com/android/nowinandroid) - Modern Android architecture showcase

### Community Resources

- [Compose CookBook](https://github.com/Gurupreet/ComposeCookBook) - Comprehensive Compose examples and patterns
- [Longboi Launcher Feedback](https://feedback.longboilauncher.com/collections/8241110-sparkles-features) - Feature ideas and user feedback

## Project Structure

```text
app/
├── src/main/java/com/longboilauncher/app/
│   ├── data/
│   │   ├── model/AppInfo.kt          # App data model
│   │   └── repository/
│   │       ├── AppRepository.kt      # App data management
│   │       └── AppLauncher.kt        # App launching logic
│   ├── ui/
│   │   ├── screens/
│   │   │   └── LauncherScreen.kt     # Main launcher UI
│   │   ├── theme/
│   │   │   ├── Theme.kt              # Material 3 theme
│   │   │   └── Type.kt               # Typography definitions
│   │   └── viewmodel/
│   │       └── LauncherViewModel.kt  # UI state management
│   ├── LauncherApplication.kt        # Application class
│   └── MainActivity.kt               # Main launcher activity
```

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Build and run on device/emulator
4. Set as home launcher when prompted

## Requirements

- Android Studio Hedgehog or later
- Android SDK 26+ (minSdkVersion)
- Kotlin 1.9.20+
- Jetpack Compose BOM 2023.10.01

## Permissions

- `QUERY_ALL_PACKAGES` - Required to enumerate all installed apps on Android 11+

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

Copyright 2024 Longboi Launcher

Licensed under the Apache License, Version 2.0
