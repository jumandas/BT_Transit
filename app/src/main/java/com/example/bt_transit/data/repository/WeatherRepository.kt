package com.example.bt_transit.data.repository

import com.example.bt_transit.data.remote.WeatherClient
import com.example.bt_transit.domain.model.WeatherInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val client: WeatherClient) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    init {
        scope.launch {
            while (true) {
                _weather.value = client.fetch()
                delay(15 * 60_000L) // refresh every 15 min
            }
        }
    }
}
