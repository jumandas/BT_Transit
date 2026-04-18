package com.example.bt_transit.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.domain.model.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    val routes: StateFlow<List<Route>> = transitRepository.observeRoutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
