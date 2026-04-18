package com.example.bt_transit.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bt_transit.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bus Arrivals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a bus is approaching your saved waypoint"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun notifyBusApproaching(waypointLabel: String, routeId: String, minutesAway: Int) {
        val text = if (minutesAway <= 1) {
            "Route $routeId is arriving at $waypointLabel now"
        } else {
            "Route $routeId arriving at $waypointLabel in $minutesAway min"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus_notification)
            .setContentTitle("Bus approaching")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(routeId.hashCode(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted yet — user will see it on next open
        }
    }

    companion object {
        const val CHANNEL_ID = "bus_arrivals"
    }
}
