package com.zzyihao.stk.data.release

data class ReleaseManifest(
    val versionName: String,
    val versionCode: Int,
    val minimumVersionCode: Int,
    val apkUrl: String,
    val sha256: String,
    val mandatory: Boolean,
    val notes: List<String>,
    val maintenance: MaintenanceNotice = MaintenanceNotice(),
) {
    companion object {}
}

data class MaintenanceNotice(
    val enabled: Boolean = false,
    val message: String = "服务维护中，请稍后再试。",
    val resumeAt: String? = null,
)
