package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.JunkCategory
import com.example.myapplication.data.models.JunkItemModel
import com.example.myapplication.data.models.JunkScanOverview
import com.example.myapplication.data.repository.CacheRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CacheUiState(
    val isScanning: Boolean = true,
    val isCleaning: Boolean = false,
    val scanOverview: JunkScanOverview = JunkScanOverview(0, 0, 0, 0, 0, 0),
    val selectedItemPaths: Set<String> = emptySet(),
    val cleanedBytesResult: Long? = null,
    val actionMessage: String? = null
) {
    val selectedTotalBytes: Long
        get() = scanOverview.items
            .filter { selectedItemPaths.contains(it.path) }
            .sumOf { it.sizeBytes }
}

class CacheViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CacheRepository(application)

    private val _uiState = MutableStateFlow(CacheUiState())
    val uiState: StateFlow<CacheUiState> = _uiState.asStateFlow()

    init {
        startScan()
    }

    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, cleanedBytesResult = null)
            val result = repository.scanJunk()
            val allPaths = result.items.map { it.path }.toSet()
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                scanOverview = result,
                selectedItemPaths = allPaths
            )
        }
    }

    fun toggleItem(path: String) {
        val current = _uiState.value.selectedItemPaths.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _uiState.value = _uiState.value.copy(selectedItemPaths = current)
    }

    fun selectAll() {
        val all = _uiState.value.scanOverview.items.map { it.path }.toSet()
        _uiState.value = _uiState.value.copy(selectedItemPaths = all)
    }

    fun deselectAll() {
        _uiState.value = _uiState.value.copy(selectedItemPaths = emptySet())
    }

    fun cleanSelectedJunk() {
        viewModelScope.launch {
            val toClean = _uiState.value.scanOverview.items.filter {
                _uiState.value.selectedItemPaths.contains(it.path)
            }
            if (toClean.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(isCleaning = true)
            delay(1000) // Brief animation time

            val freed = repository.cleanJunk(toClean)

            val remainingItems = _uiState.value.scanOverview.items.filter {
                !_uiState.value.selectedItemPaths.contains(it.path)
            }
            val newTotal = remainingItems.sumOf { it.sizeBytes }
            val newAppCache = remainingItems.filter { it.category == JunkCategory.APP_CACHE }.sumOf { it.sizeBytes }
            val newTemp = remainingItems.filter { it.category == JunkCategory.TEMP_FILE }.sumOf { it.sizeBytes }
            val newThumb = remainingItems.filter { it.category == JunkCategory.THUMBNAIL_CACHE }.sumOf { it.sizeBytes }
            val newApk = remainingItems.filter { it.category == JunkCategory.OBSOLETE_APK }.sumOf { it.sizeBytes }
            val newLog = remainingItems.filter { it.category == JunkCategory.LOG_FILE }.sumOf { it.sizeBytes }

            _uiState.value = _uiState.value.copy(
                isCleaning = false,
                cleanedBytesResult = freed,
                scanOverview = JunkScanOverview(
                    totalJunkBytes = newTotal,
                    appCacheBytes = newAppCache,
                    tempFilesBytes = newTemp,
                    thumbnailBytes = newThumb,
                    apkJunkBytes = newApk,
                    logFilesBytes = newLog,
                    items = remainingItems
                ),
                selectedItemPaths = remainingItems.map { it.path }.toSet(),
                actionMessage = "Cleaned up successfully!"
            )
        }
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(cleanedBytesResult = null)
    }
}
