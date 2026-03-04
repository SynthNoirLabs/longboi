package com.longboilauncher.app.core.common.annotations

/**
 * Marks an API as internal to the module.
 * Should not be used by other modules even if technically accessible.
 *
 * AI Agent Guardrail: When you see this annotation, do not call this API
 * from outside its module.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class InternalApi(
    val reason: String = "Internal implementation detail",
)

/**
 * Marks an API as visible for testing only.
 * Should not be used in production code.
 *
 * AI Agent Guardrail: Only use APIs with this annotation in test code.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class VisibleForTesting(
    val otherwise: Visibility = Visibility.PRIVATE,
)

/**
 * Visibility level for [VisibleForTesting].
 */
enum class Visibility {
    PRIVATE,
    PACKAGE_PRIVATE,
    PROTECTED,
    INTERNAL,
}

/**
 * Marks an API as experimental.
 * May change or be removed in future versions.
 *
 * AI Agent Guardrail: Warn user before using experimental APIs.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    message = "This API is experimental and may change.",
    level = RequiresOptIn.Level.WARNING,
)
@MustBeDocumented
annotation class ExperimentalLongboiApi

/**
 * Marks an API as deprecated with migration path.
 *
 * AI Agent Guardrail: Do not use deprecated APIs in new code.
 * Follow the migration path specified.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DeprecatedApi(
    val message: String,
    val replaceWith: String = "",
    val removeIn: String = "",
)
