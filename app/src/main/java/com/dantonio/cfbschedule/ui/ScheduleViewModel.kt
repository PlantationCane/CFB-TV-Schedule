package com.dantonio.cfbschedule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantonio.cfbschedule.data.ScheduleFilter
import com.dantonio.cfbschedule.data.ScheduleRepository
import com.dantonio.cfbschedule.data.WeekSchedule
import com.dantonio.cfbschedule.data.availableFbsConferences
import com.dantonio.cfbschedule.data.applyFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState
    data class Success(val schedule: WeekSchedule) : ScheduleUiState
    data class Error(val message: String) : ScheduleUiState
}

class ScheduleViewModel(
    private val repository: ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(ScheduleFilter())
    val filter: StateFlow<ScheduleFilter> = _filter.asStateFlow()

    /** The schedule actually shown to the user: full data with the current filter applied. */
    val displayState: StateFlow<ScheduleUiState> = combine(_uiState, _filter) { state, filter ->
        if (state is ScheduleUiState.Success) {
            ScheduleUiState.Success(state.schedule.applyFilter(filter))
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState.Loading)

    /** Conferences available to filter by, from the unfiltered week's games. */
    val availableConferences: StateFlow<List<String>> = _uiState
        .map { state -> (state as? ScheduleUiState.Success)?.schedule?.availableFbsConferences() ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCurrentWeek()
    }

    fun loadCurrentWeek() {
        _uiState.value = ScheduleUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ScheduleUiState.Success(repository.getCurrentWeekSchedule())
            } catch (e: Exception) {
                ScheduleUiState.Error(e.message ?: "Something went wrong loading the schedule.")
            }
        }
    }

    fun setTopRankOnly(enabled: Boolean) {
        _filter.value = _filter.value.copy(topRankOnly = enabled)
    }

    fun setConferenceFilter(conference: String?) {
        _filter.value = _filter.value.copy(conference = conference)
    }

    fun setShowStreamingOnly(enabled: Boolean) {
        _filter.value = _filter.value.copy(showStreamingOnly = enabled)
    }
}
