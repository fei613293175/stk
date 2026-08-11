package com.zzyihao.stk.data.release

data class ReleaseManifest(
    val versionName: String,
    val versionCode: Int,
    val minimumVersionCode: Int,
    val apkUrl: String,
    val sha256: String,
    val mandatory: Boolean,
    val notes: List<String>,
    val apkSizeBytes: Long? = null,
    val maintenance: MaintenanceNotice = MaintenanceNotice(),
) {
    companion object {}
}

object ReleaseSecurity {
    val manifestHosts = setOf("stk.zz-yihao.com", "stk-api.zz-yihao.com")
    val downloadHosts = setOf("stk-download.zz-yihao.com", "stk.zz-yihao.com")
    const val maxApkBytes = 200L * 1024L * 1024L
}

data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long?,
) {
    val percent: Int? = totalBytes?.takeIf { it > 0 }
        ?.let { ((downloadedBytes * 100L / it).coerceIn(0L, 100L)).toInt() }
}

data class MaintenanceNotice(
    val enabled: Boolean = false,
    val message: String = "服务维护中，请稍后再试。",
    val resumeAt: String? = null,
)
