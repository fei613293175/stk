package com.zzyihao.stk.data.release

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed interface InstallResult {
    data object Started : InstallResult
    data object PermissionRequired : InstallResult
}

class ApkUpdateManager(context: Context) {
    private val appContext = context.applicationContext
    private val allowedHosts = setOf("stk.zz-yihao.com", "stk-download.zz-yihao.com")

    suspend fun download(release: ReleaseManifest, onProgress: (Int?) -> Unit): File = withContext(Dispatchers.IO) {
        val source = URL(release.apkUrl)
        require(source.protocol == "https" && source.host.lowercase() in allowedHosts) { "安装包地址不在官方白名单" }
        val connection = source.openConnection() as HttpURLConnection
        val directory = File(appContext.cacheDir, "stk-updates").apply { mkdirs() }
        val partFile = File(directory, "STK-${release.versionCode}.apk.part")
        val apkFile = File(directory, "STK-${release.versionCode}.apk")
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/vnd.android.package-archive,application/octet-stream")
            connection.connect()
            val finalUrl = connection.url
            if (finalUrl.protocol != "https" || finalUrl.host.lowercase() !in allowedHosts) throw ReleaseException("下载重定向不在官方白名单")
            if (connection.responseCode !in 200..299) throw ReleaseException("安装包下载失败（HTTP ${connection.responseCode}）")
            val contentLength = connection.contentLength.toLong()
            if (contentLength > MAX_APK_BYTES) throw ReleaseException("安装包超过允许大小")
            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0L
            partFile.outputStream().buffered().use { output ->
                connection.inputStream.buffered().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_APK_BYTES) throw ReleaseException("安装包超过允许大小")
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        onProgress(if (contentLength > 0) ((total * 100 / contentLength).coerceIn(0, 100)).toInt() else null)
                    }
                }
            }
            if (contentLength > 0 && total != contentLength) throw ReleaseException("安装包下载不完整")
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actualHash.equals(release.sha256, ignoreCase = true)) throw ReleaseException("安装包 SHA-256 校验失败")
            if (apkFile.exists() && !apkFile.delete()) throw ReleaseException("无法替换旧安装包")
            if (!partFile.renameTo(apkFile)) throw ReleaseException("无法保存已校验安装包")
            apkFile
        } finally {
            connection.disconnect()
            if (partFile.exists()) partFile.delete()
        }
    }

    fun install(apkFile: File): InstallResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !appContext.packageManager.canRequestPackageInstalls()) {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(settingsIntent)
            return InstallResult.PermissionRequired
        }
        val apkUri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        appContext.startActivity(installIntent)
        return InstallResult.Started
    }

    companion object {
        private const val MAX_APK_BYTES = 200L * 1024L * 1024L
    }
}
