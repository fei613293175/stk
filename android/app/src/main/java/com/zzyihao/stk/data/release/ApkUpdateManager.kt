package com.zzyihao.stk.data.release

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed interface InstallResult {
    data object Started : InstallResult
    data object PermissionRequired : InstallResult
    data class Blocked(val message: String) : InstallResult
}

class ApkUpdateManager(context: Context) {
    private val appContext = context.applicationContext
    suspend fun download(release: ReleaseManifest, onProgress: (DownloadProgress) -> Unit): File = withContext(Dispatchers.IO) {
        val source = URL(release.apkUrl)
        requireOfficialHttpsUrl(source, ReleaseSecurity.downloadHosts, "安装包地址")
        val directory = File(appContext.cacheDir, "stk-updates").apply { mkdirs() }
        val partFile = File(directory, "STK-${release.versionCode}.apk.part")
        val apkFile = File(directory, "STK-${release.versionCode}.apk")
        directory.listFiles()?.forEach { candidate ->
            if (candidate != partFile && candidate != apkFile) candidate.delete()
        }
        partFile.delete()
        apkFile.delete()
        var connection: HttpURLConnection? = null
        try {
            var currentUrl = source
            var redirectCount = 0
            while (true) {
                requireOfficialHttpsUrl(currentUrl, ReleaseSecurity.downloadHosts, "安装包地址")
                connection = (currentUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 30_000
                    instanceFollowRedirects = false
                    setRequestProperty("Accept", "application/vnd.android.package-archive,application/octet-stream")
                    connect()
                }
                val status = connection!!.responseCode
                if (status in 300..399) {
                    if (redirectCount >= MAX_REDIRECTS) throw ReleaseException("安装包下载重定向次数过多")
                    val location = connection!!.getHeaderField("Location")?.trim().orEmpty()
                    if (location.isBlank()) throw ReleaseException("安装包下载重定向无效")
                    currentUrl = URL(currentUrl, location)
                    connection!!.disconnect()
                    connection = null
                    redirectCount++
                } else {
                    break
                }
            }
            val activeConnection = connection ?: throw ReleaseException("安装包下载连接失败")
            if (activeConnection.responseCode !in 200..299) throw ReleaseException("安装包下载失败（HTTP ${activeConnection.responseCode}）")
            val contentLength = activeConnection.contentLengthLong.takeIf { it > 0 }
            if (contentLength != null && contentLength > ReleaseSecurity.maxApkBytes) throw ReleaseException("安装包超过允许大小")
            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0L
            partFile.outputStream().buffered().use { output ->
                activeConnection.inputStream.buffered().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > ReleaseSecurity.maxApkBytes) throw ReleaseException("安装包超过允许大小")
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        onProgress(DownloadProgress(total, contentLength))
                    }
                }
            }
            if (contentLength != null && total != contentLength) throw ReleaseException("安装包下载不完整")
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actualHash.equals(release.sha256, ignoreCase = true)) throw ReleaseException("安装包 SHA-256 校验失败")
            if (apkFile.exists() && !apkFile.delete()) throw ReleaseException("无法替换旧安装包")
            if (!partFile.renameTo(apkFile)) throw ReleaseException("无法保存已校验安装包")
            apkFile
        } finally {
            connection?.disconnect()
            if (partFile.exists()) partFile.delete()
        }
    }

    fun install(apkFile: File): InstallResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !appContext.packageManager.canRequestPackageInstalls()) {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return if (settingsIntent.resolveActivity(appContext.packageManager) != null) {
                runCatching { appContext.startActivity(settingsIntent) }
                    .fold({ InstallResult.PermissionRequired }, { InstallResult.Blocked("无法打开未知应用安装授权页面") })
            } else {
                InstallResult.Blocked("系统未提供未知应用安装授权页面")
            }
        }
        if (!apkFile.isFile || apkFile.length() <= 0L) return InstallResult.Blocked("已校验安装包不存在，请重新下载")
        val apkUri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (installIntent.resolveActivity(appContext.packageManager) == null) return InstallResult.Blocked("系统安装器不可用")
        return runCatching { appContext.startActivity(installIntent) }
            .fold({ InstallResult.Started }, { InstallResult.Blocked("无法启动系统安装器") })
    }

    companion object {
        private const val MAX_REDIRECTS = 3
    }
}
