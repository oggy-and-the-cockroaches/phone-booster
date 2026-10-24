package com.example.myapplication.data.models

enum class JunkCategory {
    APP_CACHE,
    TEMP_FILE,
    THUMBNAIL_CACHE,
    OBSOLETE_APK,
    LOG_FILE,
    EMPTY_FOLDER
}

data class JunkItemModel(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val category: JunkCategory,
    val description: String,
    val isSelected: Boolean = true
)

data class JunkScanOverview(
    val totalJunkBytes: Long,
    val appCacheBytes: Long,
    val tempFilesBytes: Long,
    val thumbnailBytes: Long,
    val apkJunkBytes: Long,
    val logFilesBytes: Long,
    val items: List<JunkItemModel> = emptyList()
)
