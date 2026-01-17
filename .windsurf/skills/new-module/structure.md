# Feature Module Structure

```
feature/{name}/
├── build.gradle
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   └── java/com/longboilauncher/app/feature/{name}/
    │       ├── {Name}Screen.kt        # Stateless Composable
    │       ├── {Name}ViewModel.kt     # UDF ViewModel
    │       ├── {Name}UiState.kt       # State data class
    │       ├── {Name}Event.kt         # Event sealed interface
    │       └── di/
    │           └── {Name}Module.kt    # Hilt module (if needed)
    └── test/
        └── java/com/longboilauncher/app/feature/{name}/
            └── {Name}ViewModelTest.kt
```
