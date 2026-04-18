package com.example.bt_transit.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.domain.model.ServiceAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    repo: RealtimeRepository
) : ViewModel() {

    val alerts: StateFlow<List<ServiceAlert>> = repo.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
