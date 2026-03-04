package com.longboilauncher.app.core.common.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilities for memory management and monitoring.
 */
@Singleton
class MemoryUtils
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        /**
         * Get current memory info.
         */
        fun getMemoryInfo(): MemoryInfo {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)

            val runtime = Runtime.getRuntime()
            val nativeHeap = Debug.getNativeHeapAllocatedSize()

            return MemoryInfo(
                availableMemoryMb = memInfo.availMem / (1024 * 1024),
                totalMemoryMb = memInfo.totalMem / (1024 * 1024),
                isLowMemory = memInfo.lowMemory,
                javaHeapUsedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                javaHeapMaxMb = runtime.maxMemory() / (1024 * 1024),
                nativeHeapMb = nativeHeap / (1024 * 1024),
            )
        }

        /**
         * Check if device is in low memory condition.
         */
        fun isLowMemory(): Boolean {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            return memInfo.lowMemory
        }

        /**
         * Get memory class (max heap size in MB).
         */
        fun getMemoryClass(): Int = activityManager.memoryClass

        /**
         * Get large memory class for apps that request largeHeap.
         */
        fun getLargeMemoryClass(): Int = activityManager.largeMemoryClass

        /**
         * Log current memory state for debugging.
         */
        fun logMemoryState(tag: String = TAG) {
            val info = getMemoryInfo()
            Log.d(
                tag,
                """
            |Memory State:
            |  Available: ${info.availableMemoryMb}MB / ${info.totalMemoryMb}MB
            |  Low Memory: ${info.isLowMemory}
            |  Java Heap: ${info.javaHeapUsedMb}MB / ${info.javaHeapMaxMb}MB
            |  Native Heap: ${info.nativeHeapMb}MB
            |  Memory Class: ${getMemoryClass()}MB
                """.trimMargin(),
            )
        }

        /**
         * Suggest GC if memory usage is high.
         * Note: This is a hint, not a guarantee.
         */
        fun suggestGcIfNeeded() {
            val info = getMemoryInfo()
            val heapUsagePercent = (info.javaHeapUsedMb * 100) / info.javaHeapMaxMb

            if (heapUsagePercent > HIGH_MEMORY_THRESHOLD_PERCENT) {
                Log.d(TAG, "Heap usage at $heapUsagePercent%, suggesting GC")
            }
        }

        data class MemoryInfo(
            val availableMemoryMb: Long,
            val totalMemoryMb: Long,
            val isLowMemory: Boolean,
            val javaHeapUsedMb: Long,
            val javaHeapMaxMb: Long,
            val nativeHeapMb: Long,
        ) {
            val heapUsagePercent: Int
                get() = ((javaHeapUsedMb * 100) / javaHeapMaxMb).toInt()
        }

        companion object {
            private const val TAG = "MemoryUtils"
            private const val HIGH_MEMORY_THRESHOLD_PERCENT = 80
        }
    }
