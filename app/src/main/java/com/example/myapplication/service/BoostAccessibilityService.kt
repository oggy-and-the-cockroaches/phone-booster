package com.example.myapplication.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.LinkedList
import java.util.Queue

class BoostAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BoostA11y"

        var isServiceRunning = false
            private set

        private var instance: BoostAccessibilityService? = null
        private val queue: Queue<String> = LinkedList()
        private var isProcessing = false
        private var currentPackage: String? = null
        private val mainHandler = Handler(Looper.getMainLooper())
        private var watchdogRunnable: Runnable? = null

        fun stopPackages(context: Context, packages: List<String>) {
            Log.d(TAG, "stopPackages requested for ${packages.size} apps. Service running=$isServiceRunning")
            if (!isServiceRunning || instance == null) {
                packages.firstOrNull()?.let { openAppDetails(context, it) }
                return
            }

            queue.clear()
            queue.addAll(packages)
            processNext()
        }

        fun stopPackage(context: Context, packageName: String) {
            Log.d(TAG, "stopPackage requested for $packageName. Service running=$isServiceRunning")
            if (!isServiceRunning || instance == null) {
                openAppDetails(context, packageName)
                return
            }

            queue.clear()
            queue.add(packageName)
            processNext()
        }

        private fun processNext() {
            cancelWatchdog()

            if (queue.isEmpty()) {
                isProcessing = false
                currentPackage = null
                Log.d(TAG, "Batch processing complete.")
                return
            }

            isProcessing = true
            val pkg = queue.poll() ?: return
            currentPackage = pkg
            Log.d(TAG, "Processing package: $pkg (${queue.size} remaining)")

            val service = instance
            if (service == null) {
                isProcessing = false
                return
            }

            openAppDetails(service, pkg)

            // Watchdog: If view never opens or button isn't clickable within 1800ms, go back and continue
            watchdogRunnable = Runnable {
                if (isProcessing && currentPackage == pkg) {
                    Log.w(TAG, "Watchdog timed out for $pkg, returning back")
                    service.performGlobalAction(GLOBAL_ACTION_BACK)
                    mainHandler.postDelayed({ processNext() }, 300)
                }
            }
            mainHandler.postDelayed(watchdogRunnable!!, 1800)
        }

        private fun cancelWatchdog() {
            watchdogRunnable?.let { mainHandler.removeCallbacks(it) }
            watchdogRunnable = null
        }

        fun openAppDetails(context: Context, packageName: String) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open app details: ${e.message}")
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true
        Log.i(TAG, "BoostAccessibilityService connected successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        instance = null
        isProcessing = false
        cancelWatchdog()
        Log.i(TAG, "BoostAccessibilityService destroyed")
    }

    override fun onInterrupt() {
        isProcessing = false
        cancelWatchdog()
        queue.clear()
        Log.w(TAG, "BoostAccessibilityService interrupted")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isProcessing || event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        // Only react to Settings or System dialog windows
        if (!pkgName.contains("settings", ignoreCase = true) &&
            !pkgName.contains("systemui", ignoreCase = true) &&
            !pkgName.contains("android", ignoreCase = true)) {
            return
        }

        val rootNode = rootInActiveWindow ?: return

        // 1. Check for Confirmation Dialog first (e.g., "Force stop?")
        val dialogConfirmed = clickConfirmDialog(rootNode)
        if (dialogConfirmed) {
            Log.d(TAG, "Confirmation dialog clicked for $currentPackage")
            cancelWatchdog()
            mainHandler.postDelayed({
                performGlobalAction(GLOBAL_ACTION_BACK)
                mainHandler.postDelayed({ processNext() }, 250)
            }, 250)
            return
        }

        // 2. Check for Force Stop Button
        val result = clickForceStopButton(rootNode)
        if (result == ButtonActionState.CLICKED) {
            Log.d(TAG, "Force Stop button clicked for $currentPackage, waiting for dialog")
            // Wait for dialog popup
            mainHandler.postDelayed({
                val newRoot = rootInActiveWindow ?: return@postDelayed
                val confirmed = clickConfirmDialog(newRoot)
                if (confirmed) {
                    cancelWatchdog()
                    mainHandler.postDelayed({
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        mainHandler.postDelayed({ processNext() }, 250)
                    }, 200)
                }
            }, 300)
        } else if (result == ButtonActionState.DISABLED) {
            // App is already stopped!
            Log.d(TAG, "Force Stop button already disabled for $currentPackage (already killed)")
            cancelWatchdog()
            mainHandler.postDelayed({
                performGlobalAction(GLOBAL_ACTION_BACK)
                mainHandler.postDelayed({ processNext() }, 200)
            }, 200)
        }
    }

    private enum class ButtonActionState {
        NOT_FOUND,
        DISABLED,
        CLICKED
    }

    private fun clickForceStopButton(node: AccessibilityNodeInfo): ButtonActionState {
        val targetTexts = listOf(
            "Force stop", "Force Stop", "force stop", "FORCE STOP",
            "Stop", "STOP", "强行停止", "停止运行", "Force close"
        )
        for (text in targetTexts) {
            val nodes = node.findAccessibilityNodeInfosByText(text)
            for (n in nodes) {
                if (!n.isEnabled) {
                    return ButtonActionState.DISABLED
                }
                if (n.isClickable) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return ButtonActionState.CLICKED
                }
                val parent = n.parent
                if (parent != null) {
                    if (!parent.isEnabled) return ButtonActionState.DISABLED
                    if (parent.isClickable) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return ButtonActionState.CLICKED
                    }
                }
            }
        }

        val targetIds = listOf(
            "com.android.settings:id/force_stop_button",
            "com.android.settings:id/right_button",
            "com.android.settings:id/button2",
            "com.vivo.settings:id/force_stop_button",
            "com.vivo.settings:id/right_btn",
            "com.vivo.settings:id/stop_button"
        )
        for (resId in targetIds) {
            val nodes = node.findAccessibilityNodeInfosByViewId(resId)
            for (n in nodes) {
                if (!n.isEnabled) {
                    return ButtonActionState.DISABLED
                }
                if (n.isClickable) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return ButtonActionState.CLICKED
                }
            }
        }

        return ButtonActionState.NOT_FOUND
    }

    private fun clickConfirmDialog(node: AccessibilityNodeInfo): Boolean {
        val confirmTexts = listOf("OK", "Ok", "ok", "Force stop", "Force Stop", "确定", "强行停止")
        for (text in confirmTexts) {
            val nodes = node.findAccessibilityNodeInfosByText(text)
            for (n in nodes) {
                if (n.isClickable && n.isEnabled) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                val parent = n.parent
                if (parent != null && parent.isClickable && parent.isEnabled) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }

        val buttonIds = listOf("android:id/button1", "com.android.settings:id/button1")
        for (resId in buttonIds) {
            val nodes = node.findAccessibilityNodeInfosByViewId(resId)
            for (n in nodes) {
                if (n.isClickable && n.isEnabled) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }

        return false
    }
}

