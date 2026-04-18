package com.example.bt_transit

import android.app.Application
import android.util.Log
import com.example.bt_transit.data.local.BTDatabase
import com.example.bt_transit.data.repository.RealtimeRepository
import com.example.bt_transit.data.repository.TransitRepository
import com.example.bt_transit.notifications.ArrivalWatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class BTApplication : Application() {

    @Inject lateinit var transitRepo: TransitRepository
    @Inject lateinit var realtimeRepo: RealtimeRepository
    @Inject lateinit var db: BTDatabase
    @Inject lateinit var arrivalWatcher: ArrivalWatcher

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // TEMP: verifies the pipeline end-to-end until M3 wires a proper first-launch flow.
        scope.launch {
            try {
                if (!transitRepo.isSynced()) {
                    val ms = measureTimeMillis { transitRepo.syncStaticFeed() }
                    Log.i(TAG, "Static sync complete in ${ms}ms")
                } else {
                    Log.i(TAG, "Static feed already cached; skipping sync")
                }
                Log.i(
                    TAG,
                    "Room counts -> stops=${db.stopDao().count()} " +
                        "(expect ~300 for BT)"
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Static sync failed", t)
            }
        }
        realtimeRepo.vehicles
            .take(3)
            .onEach { list -> Log.i(TAG, "vehicles tick: ${list.size}") }
            .launchIn(scope)

        arrivalWatcher.start()
    }

    companion object {
        private const val TAG = "BTBoot"
    }
}
