package com.zzyihao.stk.data.release

import com.zzyihao.stk.ui.system.ReleaseUpdateViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseRepositoryTest {
    @Test
    fun parsesReleaseAndMaintenanceContract() {
        val release = ReleaseManifest.fromJson(
            """
            {
              "version_name":"1.4.1",
              "version_code":10401,
              "minimum_version_code":10400,
              "apk_url":"https://stk-download.zz-yihao.com/app/STK-1.4.1.apk",
              "sha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
              "apk_size_bytes":19503514,
              "mandatory":false,
              "notes":["修复启动更新检查"],
              "maintenance":{"enabled":true,"message":"例行维护","resume_at":"2026-08-08 02:00"}
            }
            """.trimIndent(),
        )

        assertEquals("1.4.1", release.versionName)
        assertEquals(10401, release.versionCode)
        assertEquals(10400, release.minimumVersionCode)
        assertEquals(19503514L, release.apkSizeBytes)
        assertTrue(release.maintenance.enabled)
        assertEquals("例行维护", release.maintenance.message)
        assertEquals("2026-08-08 02:00", release.maintenance.resumeAt)
    }

    @Test
    fun rejectsAnInsecureDownloadAddress() {
        val error = runCatching {
            ReleaseManifest.fromJson(
                """{"version_name":"1.4.1","version_code":10401,"apk_url":"http://download.invalid/app.apk","sha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}""",
            )
        }.exceptionOrNull()

        assertTrue(error is ReleaseException)
        assertFalse(error?.message.isNullOrBlank())
    }

    @Test
    fun parsesDiscuzReleaseEnvelope() {
        val release = ReleaseManifest.fromJson(
            """
            {"code":0,"message":"ok","data":{"version_name":"1.4.1","version_code":10402,"minimum_version_code":10401,"apk_url":"https://stk.zz-yihao.com/stk-release/app/STK-1.4.1.apk","sha256":"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb","mandatory":false,"notes":[]}}
            """.trimIndent(),
        )
        assertEquals("1.4.1", release.versionName)
        assertEquals(10402, release.versionCode)
    }

    @Test
    fun rejectsDownloadHostOutsideAllowlist() {
        val error = runCatching {
            ReleaseManifest.fromJson(
                """{"version_name":"1.4.1","version_code":10402,"apk_url":"https://download.invalid/app.apk","sha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}""",
            )
        }.exceptionOrNull()

        assertTrue(error is ReleaseException)
        assertTrue(error?.message?.contains("白名单") == true)
    }

    @Test
    fun calculatesByteAwareDownloadProgress() {
        val progress = DownloadProgress(downloadedBytes = 6L, totalBytes = 10L)
        assertEquals(60, progress.percent)
        assertEquals(null, DownloadProgress(downloadedBytes = 6L, totalBytes = null).percent)
    }

    @Test
    fun genericDownloadFailureMessageDoesNotExposeTransportDetails() {
        val message = ReleaseUpdateViewModel.GENERIC_DOWNLOAD_FAILURE_MESSAGE

        assertEquals("安装包下载失败，请检查网络连接或存储空间后重试。", message)
        assertFalse(message.contains("Unable to resolve host"))
        assertFalse(message.contains("stk.zz-yihao.com"))
    }
}
