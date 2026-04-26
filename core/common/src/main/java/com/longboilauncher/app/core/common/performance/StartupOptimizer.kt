package com.longboilauncher.app.core.common.performance

import android.os.Handler
import android.os.Looper
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utilities for optimizing app startup time.
 * Defers non-critical initialization to after the first frame is drawn.
 */
@Singleton
class StartupOptimizer
    @Inject
    constructor() {
        private val mainHandler = Handler(Looper.getMainLooper())
        private val deferredTasks = mutableListOf<DeferredTask>()
        private var isPostStartupPhase = false

        /**
         * Schedule a task to run after the first frame is drawn.
         * Use for non-critical initialization that can be deferred.
         */
        fun runAfterFirstFrame(
            name: String,
            priority: Priority = Priority.NORMAL,
            task: () -> Unit,
        ) {
            if (isPostStartupPhase) {
                // Already past startup, run immediately
                task()
                return
            }

            deferredTasks.add(DeferredTask(name, priority, task))
        }

        /**
         * Run a task on a background thread after startup.
         * Use for I/O or CPU-intensive initialization.
         */
        fun runInBackground(
            name: String,
            scope: CoroutineScope,
            task: suspend () -> Unit,
        ) {
            if (isPostStartupPhase) {
                scope.launch(Dispatchers.Default) { task() }
                return
            }

            runAfterFirstFrame(name, Priority.LOW) {
                scope.launch(Dispatchers.Default) { task() }
            }
        }

        /**
         * Call this after the first frame is drawn (e.g., in Activity.onResume).
         * Executes all deferred tasks in priority order.
         */
        fun onFirstFrameDrawn() {
            if (isPostStartupPhase) return
            isPostStartupPhase = true

            val sortedTasks = deferredTasks.sortedBy { it.priority.ordinal }

            Log.d(TAG, "Executing ${sortedTasks.size} deferred startup tasks")

            sortedTasks.forEachIndexed { index, task ->
                // Spread tasks across multiple frames to avoid jank
                mainHandler.postDelayed({
                    val startTime = System.currentTimeMillis()
                    try {
                        task.task()
                        val duration = System.currentTimeMillis() - startTime
                        Log.d(TAG, "Task '${task.name}' completed in ${duration}ms")
                    } catch (e: Exception) {
                        Log.e(TAG, "Task '${task.name}' failed", e)
                    }
                }, (index * TASK_DELAY_MS).toLong())
            }

            deferredTasks.clear()
        }

        /**
         * Reset for testing purposes.
         */
        fun reset() {
            isPostStartupPhase = false
            deferredTasks.clear()
        }

        enum class Priority {
            HIGH, // Run as soon as possible after first frame
            NORMAL, // Run after high priority tasks
            LOW, // Run last, can be delayed further
        }

        private data class DeferredTask(
            val name: String,
            val priority: Priority,
            val task: () -> Unit,
        )

        companion object {
            private const val TAG = "StartupOptimizer"
            private const val TASK_DELAY_MS = 16 // ~1 frame at 60fps
        }
    }
