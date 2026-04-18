package com.example.bt_transit.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GtfsStaticClient @Inject constructor(
    private val http: OkHttpClient
) {
    suspend fun download(): Map<String, List<Map<String, String>>> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(ZIP_URL).build()
        http.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "GTFS download failed: HTTP ${response.code}" }
            val body = response.body ?: error("Empty response body")
            val result = mutableMapOf<String, List<Map<String, String>>>()
            ZipInputStream(body.byteStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".txt")) {
                        // ZipInputStream reader must NOT be closed (it would close the zip stream).
                        val reader = zip.bufferedReader(Charsets.UTF_8)
                        result[entry.name] = parseCsv(reader)
                    }
                    entry = zip.nextEntry
                }
            }
            result
        }
    }

    private fun parseCsv(reader: BufferedReader): List<Map<String, String>> {
        // Must not close this reader: it wraps the ZipInputStream we still need for the next entry.
        val headerLine = reader.readLine() ?: return emptyList()
        val header = parseCsvLine(headerLine.removePrefix("\uFEFF"))
        val rows = mutableListOf<Map<String, String>>()
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isBlank()) continue
            val values = parseCsvLine(line)
            rows.add(header.mapIndexed { i, key -> key to (values.getOrNull(i) ?: "") }.toMap())
        }
        return rows
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val buf = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    buf.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(buf.toString())
                    buf.clear()
                }
                else -> buf.append(c)
            }
            i++
        }
        result.add(buf.toString())
        return result
    }

    companion object {
        const val ZIP_URL =
            "https://s3.amazonaws.com/etatransit.gtfs/bloomingtontransit.etaspot.net/gtfs.zip"
    }
}
