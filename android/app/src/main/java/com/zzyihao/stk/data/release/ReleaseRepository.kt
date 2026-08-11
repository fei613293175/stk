package com.zzyihao.stk.data.release

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

/** Public, unauthenticated release contract hosted by the download origin. */
interface ReleaseRepository {
    suspend fun getCurrentRelease(): ReleaseManifest
}

class ReleaseException(
    override val message: String,
    val requestId: String? = null,
) : Exception(message)

class HttpReleaseRepository(private val manifestUrl: String) : ReleaseRepository {
    override suspend fun getCurrentRelease(): ReleaseManifest = withContext(Dispatchers.IO) {
        val source = URL(manifestUrl)
        requireOfficialHttpsUrl(source, ReleaseSecurity.manifestHosts, "版本服务地址")
        val connection = (source.openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 4_000
            connection.readTimeout = 4_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status in 300..399) throw ReleaseException("版本服务不允许跳转，请稍后重试")
            if (status !in 200..299) throw ReleaseException("更新信息暂时不可用（HTTP $status）")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            ReleaseManifest.fromJson(body)
        } catch (error: ReleaseException) {
            throw error
        } catch (_: Exception) {
            throw ReleaseException("更新信息暂时不可用，请检查网络连接后重试")
        } finally {
            connection.disconnect()
        }
    }
}

internal fun ReleaseManifest.Companion.fromJson(source: String): ReleaseManifest {
    val root = try { Json.parseToJsonElement(source) as? JsonObject } catch (_: Exception) {
        throw ReleaseException("更新信息格式无效")
    } ?: throw ReleaseException("更新信息格式无效")
    val envelopeCode = root["code"]?.jsonPrimitive?.intOrNull
    if (envelopeCode != null && envelopeCode != 0) {
        throw ReleaseException(
            root.string("message").ifBlank { "更新信息暂时不可用" },
            root.string("request_id").takeIf(String::isNotBlank),
        )
    }
    val json = (root["data"] as? JsonObject) ?: root
    val versionName = json.string("version_name")
    val versionCode = json.int("version_code")
    val apkUrl = json.string("apk_url")
    val sha256 = json.string("sha256").lowercase()
    if (versionName.isBlank() || versionCode < 1 || !sha256.matches(Regex("[a-f0-9]{64}"))) {
        throw ReleaseException("更新信息缺少必要字段")
    }
    val parsedApkUrl = try { URL(apkUrl) } catch (_: Exception) { throw ReleaseException("安装包地址无效") }
    requireOfficialHttpsUrl(parsedApkUrl, ReleaseSecurity.downloadHosts, "安装包地址")
    val notes = (json["notes"] as? JsonArray).toStringList()
    val maintenanceJson = json["maintenance"] as? JsonObject
    val maintenance = maintenanceJson?.let {
        MaintenanceNotice(
            enabled = it.boolean("enabled"),
            message = it.string("message").ifBlank { "服务维护中，请稍后再试。" },
            resumeAt = it.string("resume_at").takeIf(String::isNotBlank),
        )
    } ?: MaintenanceNotice()
    return ReleaseManifest(
        versionName = versionName,
        versionCode = versionCode,
        minimumVersionCode = json.int("minimum_version_code").coerceAtLeast(0),
        apkUrl = apkUrl,
        sha256 = sha256,
        mandatory = json.boolean("mandatory"),
        notes = notes,
        apkSizeBytes = json.long("apk_size_bytes").takeIf { it in 1..ReleaseSecurity.maxApkBytes },
        maintenance = maintenance,
    )
}

private fun JsonObject.string(key: String): String = this[key]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()

private fun JsonObject.int(key: String): Int = this[key]?.jsonPrimitive?.intOrNull ?: 0

private fun JsonObject.long(key: String): Long = this[key]?.jsonPrimitive?.longOrNull ?: 0L

private fun JsonObject.boolean(key: String): Boolean = this[key]?.jsonPrimitive?.booleanOrNull ?: false

private fun JsonArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }.filter(String::isNotBlank)
}

internal fun requireOfficialHttpsUrl(url: URL, allowedHosts: Set<String>, label: String) {
    val defaultPort = url.port == -1 || url.port == 443
    if (url.protocol.lowercase() != "https" || url.host.lowercase() !in allowedHosts || url.userInfo != null || !defaultPort) {
        throw ReleaseException("${label}不在官方 HTTPS 白名单")
    }
}
