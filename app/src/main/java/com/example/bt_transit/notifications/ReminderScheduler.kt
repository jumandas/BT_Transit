package com.example.bt_transit.notifications

import android.app.AlarmManager
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "bt_reminders"
private const val KEY_REMINDERS = "reminders"

data class ReminderInfo(
    val key: String,
    val routeShortName: String,
    val stopName: String,
    val fireAtMs: Long
)

fun scheduleReminder(context: Context, info: ReminderInfo) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = ReminderReceiver.buildIntent(context, info)
    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, info.fireAtMs, intent)
    saveReminder(context, info)
}

fun cancelReminder(context: Context, key: String) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = ReminderReceiver.buildIntent(context, ReminderInfo(key, "", "", 0L))
    am.cancel(intent)
    removeReminder(context, key)
}

fun isReminderScheduled(context: Context, key: String): Boolean =
    loadReminders(context).any { it.key == key }

fun rescheduleAllAfterBoot(context: Context) {
    val now = System.currentTimeMillis()
    loadReminders(context)
        .filter { it.fireAtMs > now }
        .forEach { scheduleReminder(context, it) }
}

internal fun saveReminder(context: Context, info: ReminderInfo) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadReminders(context).toMutableList()
    existing.removeAll { it.key == info.key }
    existing.add(info)
    val arr = JSONArray()
    existing.forEach { r ->
        arr.put(JSONObject().apply {
            put("key", r.key)
            put("route", r.routeShortName)
            put("stop", r.stopName)
            put("fireAt", r.fireAtMs)
        })
    }
    prefs.edit().putString(KEY_REMINDERS, arr.toString()).apply()
}

internal fun removeReminder(context: Context, key: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val updated = loadReminders(context).filter { it.key != key }
    val arr = JSONArray()
    updated.forEach { r ->
        arr.put(JSONObject().apply {
            put("key", r.key)
            put("route", r.routeShortName)
            put("stop", r.stopName)
            put("fireAt", r.fireAtMs)
        })
    }
    prefs.edit().putString(KEY_REMINDERS, arr.toString()).apply()
}

fun loadReminders(context: Context): List<ReminderInfo> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(KEY_REMINDERS, "[]") ?: "[]"
    val arr = try { JSONArray(json) } catch (e: Exception) { return emptyList() }
    return (0 until arr.length()).mapNotNull { i ->
        val obj = arr.optJSONObject(i) ?: return@mapNotNull null
        ReminderInfo(
            key = obj.optString("key"),
            routeShortName = obj.optString("route"),
            stopName = obj.optString("stop"),
            fireAtMs = obj.optLong("fireAt")
        )
    }
}
