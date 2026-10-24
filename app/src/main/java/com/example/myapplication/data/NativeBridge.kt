package com.example.myapplication.data

import android.util.Log

object NativeBridge {
    private const val TAG = "NativeBridge"
    private var nativeLoaded: Boolean = false

    init {
        try {
            System.loadLibrary("boostmax_native")
            nativeLoaded = true
            Log.i(TAG, "BoostMax native C++ library loaded successfully")
        } catch (t: Throwable) {
            nativeLoaded = false
            Log.w(TAG, "Native library not available, fallback engine will be used: ${t.message}")
        }
    }

    fun isNativeEngineActive(): Boolean {
        return if (nativeLoaded) {
            try {
                isNativeActive()
            } catch (t: Throwable) {
                false
            }
        } else {
            false
        }
    }

    // JNI Declarations
    private external fun isNativeActive(): Boolean
    private external fun getNativeMemInfo(): LongArray
    private external fun getCpuUsagePercent(): Double
    private external fun scanProcessesNative(): String
    private external fun killProcessNative(pid: Int): Boolean
    private external fun scanStorageNative(path: String, maxLargeFiles: Int): String
    private external fun scanJunkNative(paths: Array<String>): String
    private external fun cleanJunkNative(paths: Array<String>): Long
    private external fun deleteFileNative(path: String): Boolean

    // Safe wrappers that forward to native if available or null/fallback
    fun getMemInfoNativeSafe(): LongArray? {
        if (!nativeLoaded) return null
        return try {
            getNativeMemInfo()
        } catch (t: Throwable) {
            null
        }
    }

    fun getCpuUsageNativeSafe(): Double? {
        if (!nativeLoaded) return null
        return try {
            getCpuUsagePercent()
        } catch (t: Throwable) {
            null
        }
    }

    fun scanProcessesNativeSafe(): String? {
        if (!nativeLoaded) return null
        return try {
            scanProcessesNative()
        } catch (t: Throwable) {
            null
        }
    }

    fun killProcessNativeSafe(pid: Int): Boolean {
        if (!nativeLoaded) return false
        return try {
            killProcessNative(pid)
        } catch (t: Throwable) {
            false
        }
    }

    fun scanStorageNativeSafe(path: String, maxLargeFiles: Int): String? {
        if (!nativeLoaded) return null
        return try {
            scanStorageNative(path, maxLargeFiles)
        } catch (t: Throwable) {
            null
        }
    }

    fun scanJunkNativeSafe(paths: Array<String>): String? {
        if (!nativeLoaded) return null
        return try {
            scanJunkNative(paths)
        } catch (t: Throwable) {
            null
        }
    }

    fun cleanJunkNativeSafe(paths: Array<String>): Long? {
        if (!nativeLoaded) return null
        return try {
            cleanJunkNative(paths)
        } catch (t: Throwable) {
            null
        }
    }

    fun deleteFileNativeSafe(path: String): Boolean {
        if (!nativeLoaded) return false
        return try {
            deleteFileNative(path)
        } catch (t: Throwable) {
            false
        }
    }
}
