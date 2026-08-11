package com.zzyihao.stk.data.release

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
              "mandatory":false,
              "notes":["修复启动更新检查"],
              "maintenance":{"enabled":true,"message":"例行维护","resume_at":"2026-08-08 02:00"}
            }
            """.trimIndent(),
        )

        assertEquals("1.4.1", release.versionName)
        assertEquals(10401, release.versionCode)
        assertEquals(10400, release.minimumVersionCode)
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
            {"code":0,"message":"ok","data":{"version_name":"1.5.0","version_code":10500,"minimum_version_code":10401,"apk_url":"https://stk.zz-yihao.com/stk-release/app/STK-1.5.0.apk","sha256":"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb","mandatory":false,"notes":[]}}
            """.trimIndent(),
        )
        assertEquals("1.5.0", release.versionName)
        assertEquals(10500, release.versionCode)
    }
}
