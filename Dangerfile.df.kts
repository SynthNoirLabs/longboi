@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("systems.danger:danger-kotlin-sdk:1.3.4")

import systems.danger.kotlin.*

val danger = Danger(args)
val pr = danger.github.pullRequest
val allFiles = danger.git.modifiedFiles + danger.git.createdFiles

// ─── PR Size ────────────────────────────────────────────────────────
val additions = pr.additions ?: 0
val deletions = pr.deletions ?: 0
val totalChanges = additions + deletions

if (totalChanges > 600) {
    warn(
        "This PR has $totalChanges lines changed ($additions+/$deletions−). " +
            "Consider splitting into smaller PRs for easier review."
    )
}

// ─── PR Description ─────────────────────────────────────────────────
val body = pr.body.orEmpty()
if (body.length < 20) {
    warn("PR description is very short. Please add context about what this PR does and why.")
}

// ─── AndroidManifest Changes ────────────────────────────────────────
val manifestChanged = allFiles.any { it.contains("AndroidManifest.xml") }
if (manifestChanged) {
    warn(
        "AndroidManifest.xml was modified. Please verify:\n" +
            "- Permissions are correct and minimal\n" +
            "- Services/receivers are properly declared\n" +
            "- `tools:targetApi` is up to date"
    )
}

// ─── Missing Tests ──────────────────────────────────────────────────
val sourceFiles = allFiles.filter {
    it.endsWith(".kt") &&
        (it.contains("src/main/") || it.contains("src/commonMain/")) &&
        !it.contains("di/") &&
        !it.contains("Module.kt")
}
val testFiles = allFiles.filter {
    it.endsWith(".kt") &&
        (it.contains("src/test/") || it.contains("src/androidTest/"))
}

if (sourceFiles.isNotEmpty() && testFiles.isEmpty()) {
    warn(
        "This PR modifies ${sourceFiles.size} source file(s) but adds no tests. " +
            "Please add tests for new or changed logic."
    )
}

// ─── Gradle/Dependency Changes ──────────────────────────────────────
val gradleChanged = allFiles.any { it.endsWith("build.gradle") || it.endsWith("build.gradle.kts") }
val versionCatalogChanged = allFiles.any { it.contains("libs.versions.toml") }

if (gradleChanged || versionCatalogChanged) {
    message(
        "Build configuration changed. CI will verify the build still succeeds."
    )
}

// ─── Sensitive File Warnings ────────────────────────────────────────
val sensitivePatterns = listOf(
    ".env", "keystore", ".jks", "google-services.json",
    "local.properties", "signing", "credentials"
)
val sensitiveFiles = allFiles.filter { file ->
    sensitivePatterns.any { pattern -> file.lowercase().contains(pattern) }
}
if (sensitiveFiles.isNotEmpty()) {
    fail(
        "Potentially sensitive files detected:\n" +
            sensitiveFiles.joinToString("\n") { "- `$it`" } +
            "\n\nPlease verify these should be committed."
    )
}

// ─── LiveData / Fragment / XML Layout Detection ─────────────────────
val kotlinSourceFiles = allFiles.filter {
    it.endsWith(".kt") && it.contains("src/main/")
}

// We can only check created/modified files by name — Danger Kotlin doesn't
// expose file contents in the DSL. The CI lint step enforces deeper checks.
if (kotlinSourceFiles.any { it.contains("Fragment") }) {
    warn(
        "A file with 'Fragment' in its name was added/modified. " +
            "This project uses pure Compose — no Fragments."
    )
}

// ─── Screenshot Test Reminder ───────────────────────────────────────
val uiFiles = allFiles.filter {
    it.endsWith(".kt") && (
        it.contains("feature/") ||
            it.contains("designsystem/")
        ) && it.contains("src/main/")
}
val screenshotTestFiles = allFiles.filter { it.contains("screenshotTest/") }

if (uiFiles.isNotEmpty() && screenshotTestFiles.isEmpty()) {
    message(
        "UI files were changed but no screenshot tests were updated. " +
            "Run `./gradlew updateDebugScreenshotTest` if visual changes are expected."
    )
}
