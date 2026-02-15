package com.longboilauncher.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.ext.list.withImport
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withParentOf
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Architecture boundary tests enforced by Konsist.
 * These tests run on the full project source set and verify that the module
 * dependency rules documented in CLAUDE.md are respected.
 *
 * Run: ./gradlew :architecture-tests:test
 */
class ModuleBoundaryTest {

    // ── Module Boundary Rules ───────────────────────────────────────

    @Test
    fun `feature modules must not import from app module`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.path.contains("/feature/") }
            .flatMap { it.imports }
            .assertFalse {
                it.name.startsWith("com.longboilauncher.app.") &&
                    !it.name.startsWith("com.longboilauncher.app.core.") &&
                    !it.name.startsWith("com.longboilauncher.app.feature.")
            }
    }

    @Test
    fun `core modules must not import from feature modules`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.path.contains("/core/") }
            .flatMap { it.imports }
            .assertFalse {
                it.name.startsWith("com.longboilauncher.app.feature.")
            }
    }

    // ── No LiveData ─────────────────────────────────────────────────

    @Test
    fun `no class should use LiveData - use StateFlow instead`() {
        Konsist
            .scopeFromProject()
            .files
            .flatMap { it.imports }
            .assertFalse {
                it.name.contains("LiveData") ||
                    it.name.contains("androidx.lifecycle.MutableLiveData") ||
                    it.name.contains("androidx.lifecycle.LiveData")
            }
    }

    // ── No XML Layouts ──────────────────────────────────────────────

    @Test
    fun `no class should reference setContentView - use Compose`() {
        Konsist
            .scopeFromProject()
            .functions()
            .assertFalse {
                it.text.contains("setContentView(R.layout")
            }
    }

    // ── Hilt Conventions ────────────────────────────────────────────

    @Test
    fun `ViewModels should use @HiltViewModel annotation`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .filter { !it.name.contains("Test") && !it.name.contains("Fake") }
            .assertTrue {
                it.hasAnnotationWithName("HiltViewModel")
            }
    }

    @Test
    fun `classes with @Inject constructors in feature modules should have @AndroidEntryPoint or @HiltViewModel`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter { it.path.contains("/feature/") }
            .filter { cls ->
                cls.constructors.any { c -> c.hasAnnotationWithName("Inject") }
            }
            .assertTrue {
                it.hasAnnotationWithName("HiltViewModel") ||
                    it.hasAnnotationWithName("AndroidEntryPoint")
            }
    }

    // ── No Fragment Usage ───────────────────────────────────────────

    @Test
    fun `no source file should import Fragment - use Compose`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { !it.path.contains("/test/") && !it.path.contains("/androidTest/") }
            .flatMap { it.imports }
            .assertFalse {
                it.name.contains("androidx.fragment.app.Fragment")
            }
    }

    // ── Service Registration Reminder ───────────────────────────────

    @Test
    fun `Services should extend Android Service classes`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Service")
            .filter {
                !it.name.contains("Test") &&
                    !it.name.contains("Fake") &&
                    !it.name.contains("Mock") &&
                    it.path.contains("src/main/")
            }
            .assertTrue {
                it.hasParentWithName("Service") ||
                    it.hasParentWithName("NotificationListenerService") ||
                    it.hasParentWithName("LifecycleService") ||
                    it.hasParentWithName("AccessibilityService")
            }
    }
}
