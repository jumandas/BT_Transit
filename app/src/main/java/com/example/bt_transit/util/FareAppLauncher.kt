package com.example.bt_transit.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

const val FARE_APP_PACKAGE = "com.cubic.ctp.app" // TODO: verify package name on Play Store before shipping

fun openFareApp(context: Context, pkg: String = FARE_APP_PACKAGE) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        ?: Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
        )
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No browser or Play Store available — silently ignore
    }
}
