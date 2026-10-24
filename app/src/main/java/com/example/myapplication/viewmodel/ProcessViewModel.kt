package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.ProcessInfo
import com.example.myapplication.data.repository.ProcessRepository
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
    val actionMessage: String? = null
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
                filteredProcesses = filtered
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
            repository.killProcess(process.pid, process.packageName)
            val updated = _uiState.value.processes.filter { it.pid != process.pid }
            _uiState.value = _uiState.value.copy(
                processes = updated,
                filteredProcesses = applyFilterAndSort(updated, _uiState.value.filter, _uiState.value.sort, _uiState.value.searchQuery),
                actionMessage = "Process stopped: ${process.appLabel ?: process.name}"
            )
        }
    }

    fun killAllUserProcesses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val myPackage = getApplication<Application>().packageName
            var killedCount = 0
            for (p in _uiState.value.processes) {
                if (p.isUserApp && p.packageName != myPackage) {
                    repository.killProcess(p.pid, p.packageName)
                    killedCount++
                }
            }
            loadProcesses()
            _uiState.value = _uiState.value.copy(
                actionMessage = "Stopped $killedCount background processes!"
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
