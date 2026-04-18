package com.example.bt_transit.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.domain.model.ServiceAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repo: RealtimeRepository
) : androidx.lifecycle.ViewModel() {

    val alerts: StateFlow<List<ServiceAlert>> = repo.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    companion object {
    }
}
