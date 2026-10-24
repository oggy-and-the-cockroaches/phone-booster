package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.ProcessInfo
import com.example.myapplication.data.models.RamStats
import com.example.myapplication.data.repository.ProcessRepository
import com.example.myapplication.data.repository.SystemStatsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RamBoosterUiState(
    val isLoading: Boolean = true,
    val isBoosting: Boolean = false,
    val ramStats: RamStats = RamStats(6_000_000_000L, 2_400_000_000L, 3_600_000_000L, 60.0),
    val topApps: List<ProcessInfo> = emptyList(),
    val boostFreedBytes: Long? = null,
    val killedAppsCount: Int = 0
)

class RamBoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val statsRepo = SystemStatsRepository(application)
    private val processRepo = ProcessRepository(application)

    private val _uiState = MutableStateFlow(RamBoosterUiState())
    val uiState: StateFlow<RamBoosterUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val ram = statsRepo.getRamStats()
            val allProcs = processRepo.getRunningProcesses()
            val userProcs = allProcs.filter { it.isUserApp }
            val procs = if (userProcs.size >= 5) userProcs.take(20) else allProcs.take(20)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                ramStats = ram,
                topApps = procs
            )
        }
    }

    fun boostRam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBoosting = true, boostFreedBytes = null)
            val beforeUsed = _uiState.value.ramStats.usedBytes

            val myPackage = getApplication<Application>().packageName
            val candidateApps = _uiState.value.topApps.filter { it.packageName != myPackage }

            if (com.example.myapplication.service.BoostAccessibilityService.isServiceRunning) {
                val pkgs = candidateApps.map { it.packageName }
                com.example.myapplication.service.BoostAccessibilityService.stopPackages(getApplication(), pkgs)
            } else {
                for (app in candidateApps) {
                    processRepo.killProcess(app.pid, app.packageName)
                }
            }

            delay(1500)

            val updatedRunning = processRepo.getRunningProcesses()
            val runningPkgs = updatedRunning.map { it.packageName }.toSet()

            var actuallyKilledCount = 0
            for (app in candidateApps) {
                if (!runningPkgs.contains(app.packageName)) {
                    actuallyKilledCount++
                }
            }

            val newRam = statsRepo.getRamStats()
            val realFreed = (beforeUsed - newRam.usedBytes).coerceAtLeast(0L)
            val updatedUserApps = updatedRunning.filter { it.isUserApp }.take(10)

            _uiState.value = _uiState.value.copy(
                isBoosting = false,
                ramStats = newRam,
                topApps = if (updatedUserApps.isNotEmpty()) updatedUserApps else updatedRunning.take(10),
                boostFreedBytes = realFreed,
                killedAppsCount = actuallyKilledCount
            )
        }
    }

    fun dismissBoostResult() {
        _uiState.value = _uiState.value.copy(boostFreedBytes = null)
    }
}
