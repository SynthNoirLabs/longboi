package com.longboilauncher.app.core.common.analytics

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global uncaught exception handler that logs crashes before the app terminates.
 * Integrates with [AnalyticsManager] for crash reporting.
 */
@Singleton
class CrashHandler
    @Inject
    constructor(
        private val analyticsManager: AnalyticsManager,
    ) : Thread.UncaughtExceptionHandler {
        private var defaultHandler: Thread.UncaughtExceptionHandler? = null

        /**
         * Install this handler as the default uncaught exception handler.
         * Should be called once during app initialization.
         */
        fun install() {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(this)
            Log.d(TAG, "CrashHandler installed")
        }

        override fun uncaughtException(
            thread: Thread,
            throwable: Throwable,
        ) {
            try {
                analyticsManager.logException(
                    throwable = throwable,
                    context =
                        mapOf(
                            "thread_name" to thread.name,
                            "thread_id" to thread.id.toString(),
                            "is_main_thread" to
                                (
                                    thread ==
                                        android.os.Looper
                                            .getMainLooper()
                                            .thread
                                ).toString(),
                        ),
                )
                Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)
            } catch (e: Exception) {
                Log.e(TAG, "Error logging crash", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        companion object {
            private const val TAG = "CrashHandler"
        }
    }

/**
 * ANR (Application Not Responding) watchdog.
 * Monitors the main thread and logs if it appears blocked.
 */
class ANRWatchdog(
    private val analyticsManager: AnalyticsManager,
    private val timeoutMs: Long = 5000L,
) {
    @Volatile
    private var running = false
    private var watchThread: Thread? = null
    private var ticker = 0

    fun start() {
        if (running) return
        running = true

        watchThread =
            Thread({
                while (running) {
                    val currentTicker = ticker

                    // Post to main thread to increment ticker
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        ticker++
                    }

                    Thread.sleep(timeoutMs)

                    if (running && ticker == currentTicker) {
                        // Main thread didn't respond in time
                        val mainThread =
                            android.os.Looper
                                .getMainLooper()
                                .thread
                        val stackTrace = mainThread.stackTrace

                        analyticsManager.logBreadcrumb(
                            message = "Potential ANR detected",
                            category = "anr",
                        )
                        analyticsManager.logException(
                            throwable = ANRException(stackTrace),
                            context =
                                mapOf(
                                    "timeout_ms" to timeoutMs.toString(),
                                    "main_thread_state" to mainThread.state.name,
                                ),
                        )
                        Log.w(TAG, "Potential ANR detected! Main thread stack trace:")
                        stackTrace.forEach { element ->
                            Log.w(TAG, "  at $element")
                        }
                    }
                }
            }, "ANRWatchdog").apply {
                isDaemon = true
                start()
            }
    }

    fun stop() {
        running = false
        watchThread?.interrupt()
        watchThread = null
    }

    private class ANRException(
        stackTrace: Array<StackTraceElement>,
    ) : Exception("ANR detected") {
        init {
            setStackTrace(stackTrace)
        }
    }

    companion object {
        private const val TAG = "ANRWatchdog"
    }
}
