package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.RamStats
import com.example.myapplication.data.models.SystemOverview
import com.example.myapplication.data.repository.ProcessRepository
import com.example.myapplication.data.repository.SystemStatsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val overview: SystemOverview = SystemOverview(
        cpuUsagePercent = 14.5,
        ramStats = RamStats(6_000_000_000L, 2_400_000_000L, 3_600_000_000L, 60.0),
        totalStorageBytes = 64_000_000_000L,
        freeStorageBytes = 24_000_000_000L,
        usedStorageBytes = 40_000_000_000L,
        storageUsagePercent = 62.5
    ),
    val isBoosting: Boolean = false,
    val boostMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val statsRepo = SystemStatsRepository(application)
    private val processRepo = ProcessRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        startMetricsPolling()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val stats = statsRepo.getSystemOverview()
            _uiState.value = _uiState.value.copy(isLoading = false, overview = stats)
        }
    }

    private fun startMetricsPolling() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val stats = statsRepo.getSystemOverview()
                    _uiState.value = _uiState.value.copy(isLoading = false, overview = stats)
                } catch (e: Exception) {
                    // Ignore transient errors
                }
                delay(2500)
            }
        }
    }

    fun quickBoost() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBoosting = true, boostMessage = "Optimizing memory & processes...")
            val processes = processRepo.getRunningProcesses()
            var killedCount = 0
            for (p in processes.take(8)) {
                if (p.isUserApp && p.packageName != getApplication<Application>().packageName) {
                    processRepo.killProcess(p.pid, p.packageName)
                    killedCount++
                }
            }
            delay(1200)
            val updated = statsRepo.getSystemOverview()
            _uiState.value = _uiState.value.copy(
                isBoosting = false,
                overview = updated,
                boostMessage = "Boost Complete! Freed background slots."
            )
            delay(3000)
            _uiState.value = _uiState.value.copy(boostMessage = null)
        }
    }
}
