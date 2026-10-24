package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.ProcessInfo
import com.example.myapplication.data.repository.ProcessRepository
import com.example.myapplication.service.BoostAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ProcessFilter {
    ALL, USER_ONLY, SYSTEM_ONLY
}

enum class ProcessSort {
    MEMORY_DESC, MEMORY_ASC, NAME, PID
}

data class ProcessUiState(
    val isLoading: Boolean = true,
    val processes: List<ProcessInfo> = emptyList(),
    val filteredProcesses: List<ProcessInfo> = emptyList(),
    val filter: ProcessFilter = ProcessFilter.ALL,
    val sort: ProcessSort = ProcessSort.MEMORY_DESC,
    val searchQuery: String = "",
    val actionMessage: String? = null,
    val isAccessibilityEnabled: Boolean = false
)

class ProcessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProcessRepository(application)

    private val _uiState = MutableStateFlow(ProcessUiState())
    val uiState: StateFlow<ProcessUiState> = _uiState.asStateFlow()

    init {
        loadProcesses()
    }

    fun loadProcesses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val list = repository.getRunningProcesses()
            val filtered = applyFilterAndSort(list, _uiState.value.filter, _uiState.value.sort, _uiState.value.searchQuery)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                processes = list,
                filteredProcesses = filtered,
                isAccessibilityEnabled = BoostAccessibilityService.isServiceRunning
            )
        }
    }

    fun setFilter(filter: ProcessFilter) {
        _uiState.value = _uiState.value.copy(
            filter = filter,
            filteredProcesses = applyFilterAndSort(_uiState.value.processes, filter, _uiState.value.sort, _uiState.value.searchQuery)
        )
    }

    fun setSort(sort: ProcessSort) {
        _uiState.value = _uiState.value.copy(
            sort = sort,
            filteredProcesses = applyFilterAndSort(_uiState.value.processes, _uiState.value.filter, sort, _uiState.value.searchQuery)
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProcesses = applyFilterAndSort(_uiState.value.processes, _uiState.value.filter, _uiState.value.sort, query)
        )
    }

    fun killProcess(process: ProcessInfo) {
        viewModelScope.launch {
            val myPackage = getApplication<Application>().packageName
            if (process.packageName == myPackage) {
                _uiState.value = _uiState.value.copy(actionMessage = "Cannot terminate BoostMax app!")
                return@launch
            }

            val label = process.appLabel ?: process.name
            repository.killProcess(process.pid, process.packageName)

            if (!BoostAccessibilityService.isServiceRunning) {
                delay(400)
                if (!repository.isPackageRunning(process.packageName)) {
                    _uiState.value = _uiState.value.copy(actionMessage = "✓ $label stopped successfully!")
                } else {
                    BoostAccessibilityService.openAppDetails(getApplication(), process.packageName)
                    _uiState.value = _uiState.value.copy(
                        actionMessage = "OriginOS protected $label. Tap 'Force Stop' in settings to finish."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    actionMessage = "⚡ Automated Force-Stop executing for $label..."
                )
            }

            delay(1200)
            val updated = repository.getRunningProcesses()
            val isStillAlive = repository.isPackageRunning(process.packageName)
            val finalMsg = if (!isStillAlive) "✓ $label stopped!" else _uiState.value.actionMessage
            _uiState.value = _uiState.value.copy(
                processes = updated,
                filteredProcesses = applyFilterAndSort(updated, _uiState.value.filter, _uiState.value.sort, _uiState.value.searchQuery),
                actionMessage = finalMsg
            )
        }
    }

    fun killAllUserProcesses() {
        viewModelScope.launch {
            val myPackage = getApplication<Application>().packageName
            val targetPackages = _uiState.value.processes
                .filter { it.isUserApp && it.packageName != myPackage }
                .map { it.packageName }

            if (targetPackages.isEmpty()) {
                _uiState.value = _uiState.value.copy(actionMessage = "No user background processes to stop.")
                return@launch
            }

            if (BoostAccessibilityService.isServiceRunning) {
                _uiState.value = _uiState.value.copy(actionMessage = "⚡ Automated Force-Stop running for ${targetPackages.size} apps...")
                BoostAccessibilityService.stopPackages(getApplication(), targetPackages)
            } else {
                for (pkg in targetPackages) {
                    repository.killProcess(0, pkg)
                }
                targetPackages.firstOrNull()?.let {
                    BoostAccessibilityService.openAppDetails(getApplication(), it)
                }
                _uiState.value = _uiState.value.copy(
                    actionMessage = "Background halted. Enable 'Automated Killer' in Accessibility for 1-tap Force Stop."
                )
            }

            delay(2000)
            val updated = repository.getRunningProcesses()
            val remainingUserApps = updated.count { it.isUserApp && it.packageName != myPackage }
            val stoppedCount = targetPackages.size - remainingUserApps
            _uiState.value = _uiState.value.copy(
                processes = updated,
                filteredProcesses = applyFilterAndSort(updated, _uiState.value.filter, _uiState.value.sort, _uiState.value.searchQuery),
                actionMessage = if (stoppedCount > 0) "✓ Stopped $stoppedCount apps ($remainingUserApps protected by OriginOS)" else _uiState.value.actionMessage
            )
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }

    private fun applyFilterAndSort(
        list: List<ProcessInfo>,
        filter: ProcessFilter,
        sort: ProcessSort,
        query: String
    ): List<ProcessInfo> {
        var res = list

        if (query.isNotBlank()) {
            res = res.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true) ||
                (it.appLabel?.contains(query, ignoreCase = true) == true)
            }
        }

        res = when (filter) {
            ProcessFilter.ALL -> res
            ProcessFilter.USER_ONLY -> res.filter { it.isUserApp }
            ProcessFilter.SYSTEM_ONLY -> res.filter { !it.isUserApp }
        }

        res = when (sort) {
            ProcessSort.MEMORY_DESC -> res.sortedByDescending { it.memoryBytes }
            ProcessSort.MEMORY_ASC -> res.sortedBy { it.memoryBytes }
            ProcessSort.NAME -> res.sortedBy { (it.appLabel ?: it.name).lowercase() }
            ProcessSort.PID -> res.sortedBy { it.pid }
        }

        return res
    }
}
