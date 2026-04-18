package com.example.bt_transit

import android.app.Application
import com.example.bt_transit.data.repository.RealtimeRepository

class BTTransitApplication : Application() {
    val realtimeRepository by lazy { RealtimeRepository() }
}
