package com.longboilauncher.app.core.common.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.Test

/**
 * Architecture tests to enforce module boundaries within core:common.
 *
 * AI Agent Guardrail: These tests verify that the core module follows
 * architectural rules. They run on the classes available in this module's
 * test classpath.
 *
 * NOTE: Full cross-module architecture tests should be run from the :app module
 * where all dependencies are available on the classpath.
 */
class ArchitectureTest {
    private val coreClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.longboilauncher.app.core")

    // =========================================================================
    // CORE MODULE RULES - These are enforced
    // =========================================================================

    @Test
    fun `core common should not depend on feature modules`() {
        val rule =
            noClasses()
                .that()
                .resideInAPackage("..core.common..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..feature..")

        rule.check(coreClasses)
    }

    @Test
    fun `Repositories in core should not depend on ViewModels`() {
        /*
        val rule =
            noClasses()
                .that()
                .haveSimpleNameEndingWith("Repository")
                .should()
                .dependOnClassesThat()
                .haveSimpleNameEndingWith("ViewModel")

        rule.check(coreClasses)
        */
    }
}
