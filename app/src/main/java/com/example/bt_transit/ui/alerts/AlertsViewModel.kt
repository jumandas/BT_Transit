package com.example.bt_transit.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bt_transit.BTTransitApplication
import com.example.bt_transit.domain.model.ServiceAlert
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AlertsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as BTTransitApplication).realtimeRepository

    val alerts: StateFlow<List<ServiceAlert>> = repo.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                AlertsViewModel(app)
            }
        }
    }
}
