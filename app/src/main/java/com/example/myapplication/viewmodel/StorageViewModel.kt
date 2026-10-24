package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.AppStorageItem
import com.example.myapplication.data.models.LargeFile
import com.example.myapplication.data.models.StorageOverview
import com.example.myapplication.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StorageTab {
    CATEGORIES, LARGE_FILES, APPS
}

data class StorageUiState(
    val isLoading: Boolean = true,
    val selectedTab: StorageTab = StorageTab.CATEGORIES,
    val overview: StorageOverview = StorageOverview(
        totalBytes = 64_000_000_000L,
        usedBytes = 42_000_000_000L,
        freeBytes = 22_000_000_000L,
        imagesBytes = 14_000_000_000L,
        videosBytes = 16_500_000_000L,
        audioBytes = 3_200_000_000L,
        docsBytes = 1_800_000_000L,
        archivesBytes = 2_100_000_000L,
        otherBytes = 4_400_000_000L,
        totalFiles = 8540,
        totalDirs = 412
    ),
    val appStorageList: List<AppStorageItem> = emptyList(),
    val actionMessage: String? = null
)

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StorageRepository(application)

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init {
        loadStorageData()
    }

    fun selectTab(tab: StorageTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        if (tab == StorageTab.APPS && _uiState.value.appStorageList.isEmpty()) {
            loadAppsStorage()
        }
    }

    fun loadStorageData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val overview = repository.getStorageOverview()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                overview = overview
            )
        }
    }

    private fun loadAppsStorage() {
        viewModelScope.launch {
            val apps = repository.getInstalledAppsStorage()
            _uiState.value = _uiState.value.copy(appStorageList = apps)
        }
    }

    fun deleteLargeFile(file: LargeFile) {
        viewModelScope.launch {
            val success = repository.deleteFile(file.path)
            if (success) {
                val updatedFiles = _uiState.value.overview.largestFiles.filter { it.path != file.path }
                val updatedOverview = _uiState.value.overview.copy(
                    largestFiles = updatedFiles,
                    usedBytes = (_uiState.value.overview.usedBytes - file.sizeBytes).coerceAtLeast(0L),
                    freeBytes = _uiState.value.overview.freeBytes + file.sizeBytes
                )
                _uiState.value = _uiState.value.copy(
                    overview = updatedOverview,
                    actionMessage = "Deleted ${file.name}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    actionMessage = "Unable to delete file (read-only or permission required)"
                )
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
