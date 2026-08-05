package com.zzyihao.stk

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class StkApiClient(private val baseUrl: String = "https://stk-api.zz-yihao.com") {
    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun post(path: String, body: String, accessToken: String? = null, callback: (Result<String>) -> Unit) {
        executor.execute {
            val result = runCatching {
                val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    accessToken?.takeIf { it.isNotBlank() }?.let { setRequestProperty("Authorization", "Bearer $it") }
                }
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
                val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                if (connection.responseCode !in 200..299) error(response.ifBlank { "请求失败（${connection.responseCode}）" })
                response
            }
            mainHandler.post { callback(result) }
        }
    }

    fun get(path: String, accessToken: String? = null, callback: (Result<String>) -> Unit) {
        executor.execute {
            val result = runCatching {
                val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    accessToken?.takeIf { it.isNotBlank() }?.let { setRequestProperty("Authorization", "Bearer $it") }
                }
                val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
                val response = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                if (connection.responseCode !in 200..299) error(response.ifBlank { "请求失败（${connection.responseCode}）" })
                response
            }
            mainHandler.post { callback(result) }
        }
    }
}
