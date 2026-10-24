package com.example.myapplication.data.models

data class RamStats(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usagePercent: Double,
    val buffersBytes: Long = 0,
    val cachedBytes: Long = 0
)

data class SystemOverview(
    val cpuUsagePercent: Double,
    val ramStats: RamStats,
    val totalStorageBytes: Long,
    val freeStorageBytes: Long,
    val usedStorageBytes: Long,
    val storageUsagePercent: Double,
    val batteryLevel: Int = 100,
    val batteryTempCelsius: Double = 32.0,
    val runningProcessesCount: Int = 0,
    val isNativeEngineActive: Boolean = false
)
