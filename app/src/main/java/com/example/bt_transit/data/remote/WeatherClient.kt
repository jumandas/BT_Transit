package com.example.bt_transit.data.remote

import android.util.Log
import com.example.bt_transit.domain.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherClient @Inject constructor(private val http: OkHttpClient) {

    companion object {
        private const val URL =
            "https://api.open-meteo.com/v1/forecast" +
            "?latitude=39.1653&longitude=-86.5264" +
            "&current_weather=true&temperature_unit=fahrenheit"
    }

    suspend fun fetch(): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(URL).build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val json = JSONObject(resp.body!!.string())
                val cw = json.getJSONObject("current_weather")
                val temp = cw.getDouble("temperature").toFloat()
                val code = cw.getInt("weathercode")
                WeatherInfo(
                    tempF = temp,
                    condition = codeToCondition(code),
                    emoji = codeToEmoji(code)
                )
            }
        } catch (t: Throwable) {
            Log.w("WeatherClient", "fetch failed: ${t.message}")
            null
        }
    }

    private fun codeToCondition(code: Int): String = when (code) {
        0 -> "Clear"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        in 51..55 -> "Drizzle"
        in 61..65 -> "Rain"
        in 71..75 -> "Snow"
        in 80..82 -> "Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Cloudy"
    }

    private fun codeToEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..55 -> "🌦️"
        in 61..65 -> "🌧️"
        in 71..75 -> "🌨️"
        in 80..82 -> "🌦️"
        95, 96, 99 -> "⛈️"
        else -> "☁️"
    }
}
