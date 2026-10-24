package com.example.myapplication.data.models

import android.graphics.drawable.Drawable

data class LargeFile(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val extension: String
)

data class StorageCategory(
    val name: String,
    val bytes: Long,
    val colorHex: Long,
    val iconName: String
)

data class StorageOverview(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val imagesBytes: Long,
    val videosBytes: Long,
    val audioBytes: Long,
    val docsBytes: Long,
    val archivesBytes: Long,
    val otherBytes: Long,
    val totalFiles: Long,
    val totalDirs: Long,
    val largestFiles: List<LargeFile> = emptyList()
)

data class AppStorageItem(
    val packageName: String,
    val appName: String,
    val appSize: Long,
    val dataSize: Long,
    val cacheSize: Long,
    val totalSize: Long,
    val icon: Drawable? = null
)
