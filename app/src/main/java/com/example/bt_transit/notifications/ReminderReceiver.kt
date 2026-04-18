package com.example.bt_transit.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bt_transit.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val route = intent.getStringExtra(EXTRA_ROUTE) ?: return
        val stop = intent.getStringExtra(EXTRA_STOP) ?: return
        val key = intent.getStringExtra(EXTRA_KEY) ?: return

        val notification = NotificationCompat.Builder(context, TransitNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus_notification)
            .setContentTitle("Departure in 5 min")
            .setContentText("Route $route leaves from $stop soon")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(key.hashCode(), notification)
        } catch (e: SecurityException) {}

        removeReminder(context, key)
    }

    companion object {
        private const val EXTRA_ROUTE = "route"
        private const val EXTRA_STOP = "stop"
        private const val EXTRA_KEY = "key"

        fun buildIntent(context: Context, info: ReminderInfo): PendingIntent {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_ROUTE, info.routeShortName)
                putExtra(EXTRA_STOP, info.stopName)
                putExtra(EXTRA_KEY, info.key)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
            return PendingIntent.getBroadcast(
                context,
                info.key.hashCode(),
                intent,
                flags
            )
        }
    }
}
