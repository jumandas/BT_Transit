package com.example.bt_transit.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bt_transit.data.local.projection.DirectTripResult
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.domain.model.Route
import com.example.bt_transit.domain.model.ScheduledStop
import com.example.bt_transit.domain.model.Stop
import com.example.bt_transit.notifications.ReminderInfo
import com.example.bt_transit.notifications.cancelReminder
import com.example.bt_transit.notifications.isReminderScheduled
import com.example.bt_transit.notifications.loadReminders
import com.example.bt_transit.notifications.scheduleReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TripScheduleDetail(
    val route: Route,
    val firstDepartureTime: String,
    val stops: List<ScheduledStop>,
    val isLoading: Boolean
)

data class PlannerResult(
    val tripId: String,
    val fromStop: Stop,
    val toStop: Stop,
    val departureTime: String,
    val arrivalTime: String,
    val durationMin: Int
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val transitRepository: TransitRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val routes: StateFlow<List<Route>> = transitRepository.observeRoutes()
        .map { list ->
            list.sortedWith(compareBy(
                { it.shortName.toIntOrNull() ?: Int.MAX_VALUE },
                { it.shortName }
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // "routeId:day" -> next departure time strings (loaded on first expand)
    private val _departures = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val departures: StateFlow<Map<String, List<String>>> = _departures.asStateFlow()

    private val _selectedDay = MutableStateFlow(0) // 0 = today, 1 = tomorrow
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Currently expanded trip detail (which stops the trip hits + scheduled times)
    private val _tripDetail = MutableStateFlow<TripScheduleDetail?>(null)
    val tripDetail: StateFlow<TripScheduleDetail?> = _tripDetail.asStateFlow()

    private val _plannerFrom = MutableStateFlow<Stop?>(null)
    val plannerFrom: StateFlow<Stop?> = _plannerFrom.asStateFlow()

    private val _plannerTo = MutableStateFlow<Stop?>(null)
    val plannerTo: StateFlow<Stop?> = _plannerTo.asStateFlow()

    private val _plannerResult = MutableStateFlow<PlannerResult?>(null)
    val plannerResult: StateFlow<PlannerResult?> = _plannerResult.asStateFlow()

    private val _plannerLoading = MutableStateFlow(false)
    val plannerLoading: StateFlow<Boolean> = _plannerLoading.asStateFlow()

    private val _stopSearchResults = MutableStateFlow<List<Stop>>(emptyList())
    val stopSearchResults: StateFlow<List<Stop>> = _stopSearchResults.asStateFlow()

    private val _scheduledReminders = MutableStateFlow(
        loadReminders(context).map { it.key }.toSet()
    )
    val scheduledReminders: StateFlow<Set<String>> = _scheduledReminders.asStateFlow()

    fun setDay(day: Int) {
        require(day in 0..1)
        if (_selectedDay.value == day) return
        _selectedDay.value = day
        _departures.value = emptyMap() // invalidate cache when day changes
    }

    fun loadDeparturesFor(routeId: String) {
        val day = _selectedDay.value
        val cacheKey = "$routeId:$day"
        if (cacheKey in _departures.value) return
        viewModelScope.launch {
            val timeStr = if (day == 0) currentTimeStr() else "00:00:00"
            val times = transitRepository.getNextDeparturesForRoute(routeId, timeStr, 6)
            _departures.update { it + (cacheKey to times) }
        }
    }

    fun openTripDetail(route: Route, firstDepartureTime: String) {
        _tripDetail.value = TripScheduleDetail(route, firstDepartureTime, emptyList(), isLoading = true)
        viewModelScope.launch {
            val stops = transitRepository.getTripScheduleByFirstDeparture(
                route.routeId, firstDepartureTime
            )
            _tripDetail.value = TripScheduleDetail(route, firstDepartureTime, stops, isLoading = false)
        }
    }

    fun dismissTripDetail() {
        _tripDetail.value = null
    }

    private fun currentTimeStr(): String {
        val cal = Calendar.getInstance()
        return "%02d:%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND)
        )
    }

    fun searchStopsFor(query: String) {
        if (query.length < 2) { _stopSearchResults.value = emptyList(); return }
        viewModelScope.launch {
            _stopSearchResults.value = transitRepository.searchStops(query)
        }
    }

    fun setFromStop(stop: Stop) {
        _plannerFrom.value = stop
        _stopSearchResults.value = emptyList()
        runPlannerIfReady()
    }

    fun setToStop(stop: Stop) {
        _plannerTo.value = stop
        _stopSearchResults.value = emptyList()
        runPlannerIfReady()
    }

    fun clearPlanner() {
        _plannerFrom.value = null
        _plannerTo.value = null
        _plannerResult.value = null
        _stopSearchResults.value = emptyList()
    }

    fun clearSearchResults() {
        _stopSearchResults.value = emptyList()
    }

    private fun runPlannerIfReady() {
        val from = _plannerFrom.value ?: return
        val to = _plannerTo.value ?: return
        _plannerLoading.value = true
        _plannerResult.value = null
        viewModelScope.launch {
            try {
                val result = transitRepository.findDirectTrip(from.stopId, to.stopId, currentTimeStr())
                _plannerResult.value = if (result != null) {
                    PlannerResult(
                        tripId = result.tripId,
                        fromStop = from,
                        toStop = to,
                        departureTime = result.fromDepartureTime,
                        arrivalTime = result.toArrivalTime,
                        durationMin = minutesBetween(result.fromDepartureTime, result.toArrivalTime)
                    )
                } else null
            } finally {
                _plannerLoading.value = false
            }
        }
    }

    private fun minutesBetween(t1: String, t2: String): Int {
        fun toMinutes(t: String): Int {
            val parts = t.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return h * 60 + m
        }
        val diff = toMinutes(t2) - toMinutes(t1)
        return if (diff < 0) diff + 24 * 60 else diff
    }

    fun reminderKey(routeShortName: String, departureTime: String, selectedDay: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, selectedDay)
        val dateStr = "%04d%02d%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        return "$routeShortName|$departureTime|$dateStr"
    }

    fun toggleReminder(routeShortName: String, departureTime: String, selectedDay: Int) {
        val key = reminderKey(routeShortName, departureTime, selectedDay)
        if (isReminderScheduled(context, key)) {
            cancelReminder(context, key)
            _scheduledReminders.value = _scheduledReminders.value - key
        } else {
            val fireAtMs = departureTimeToEpochMs(departureTime, selectedDay) - 5 * 60 * 1000L
            if (fireAtMs <= System.currentTimeMillis()) return
            val info = ReminderInfo(key, routeShortName, "", fireAtMs)
            scheduleReminder(context, info)
            _scheduledReminders.value = _scheduledReminders.value + key
        }
    }

    private fun departureTimeToEpochMs(timeStr: String, dayOffset: Int): Long {
        val parts = timeStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return 0L
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, h % 24)
            set(java.util.Calendar.MINUTE, m)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (h >= 24) add(java.util.Calendar.DAY_OF_YEAR, 1)
            add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
        }
        return cal.timeInMillis
    }
}
