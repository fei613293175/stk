package com.zzyihao.stk.ui.system

import com.zzyihao.stk.data.release.MaintenanceNotice
import com.zzyihao.stk.data.release.ReleaseManifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupGateTest {
    @Test
    fun betaChannelIgnoresProductionReleaseThresholds() {
        val gate = resolveStartupGate(
            release = release(minimumVersionCode = 10504, mandatory = true),
            installedVersionCode = 10002,
            enforceProductionReleaseUpdates = false,
        )

        assertEquals(StartupGate.Continue, gate)
    }

    @Test
    fun betaChannelStillHonorsMaintenance() {
        val gate = resolveStartupGate(
            release = release(maintenance = MaintenanceNotice(enabled = true, message = "维护中")),
            installedVersionCode = 10002,
            enforceProductionReleaseUpdates = false,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.Maintenance, (gate as StartupGate.Feedback).state)
    }

    @Test
    fun productionChannelStillEnforcesMinimumVersion() {
        val gate = resolveStartupGate(
            release = release(minimumVersionCode = 10504),
            installedVersionCode = 10002,
            enforceProductionReleaseUpdates = true,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.ForcedUpdate, (gate as StartupGate.Feedback).state)
    }

    private fun release(
        minimumVersionCode: Int = 0,
        mandatory: Boolean = false,
        maintenance: MaintenanceNotice = MaintenanceNotice(),
    ) = ReleaseManifest(
        versionName = "1.5.4",
        versionCode = 10504,
        minimumVersionCode = minimumVersionCode,
        apkUrl = "https://example.com/stk.apk",
        sha256 = "a".repeat(64),
        mandatory = mandatory,
        notes = emptyList(),
        maintenance = maintenance,
    )
}
