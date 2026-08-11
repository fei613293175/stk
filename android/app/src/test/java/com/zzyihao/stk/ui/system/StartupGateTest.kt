package com.zzyihao.stk.ui.system

import com.zzyihao.stk.data.release.MaintenanceNotice
import com.zzyihao.stk.data.release.ReleaseManifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupGateTest {
    @Test
    fun betaChannelHonorsReleaseThresholds() {
        val gate = resolveStartupGate(
            release = release(minimumVersionCode = 10402, mandatory = true),
            installedVersionCode = 10002,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.ForcedUpdate, (gate as StartupGate.Feedback).state)
    }

    @Test
    fun maintenanceTakesPriorityOverReleaseThresholds() {
        val gate = resolveStartupGate(
            release = release(maintenance = MaintenanceNotice(enabled = true, message = "维护中")),
            installedVersionCode = 10002,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.Maintenance, (gate as StartupGate.Feedback).state)
    }

    @Test
    fun minimumVersionIsAlwaysEnforced() {
        val gate = resolveStartupGate(
            release = release(minimumVersionCode = 10402),
            installedVersionCode = 10002,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.ForcedUpdate, (gate as StartupGate.Feedback).state)
    }

    @Test
    fun newerReleaseProducesOptionalUpdate() {
        val gate = resolveStartupGate(
            release = release(minimumVersionCode = 10301, mandatory = false),
            installedVersionCode = 10401,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.OptionalUpdate, (gate as StartupGate.Feedback).state)
    }

    @Test
    fun currentReleaseCanRenderUpToDateForExplicitCheck() {
        val gate = resolveStartupGate(
            release = release(minimumVersionCode = 10301, mandatory = false),
            installedVersionCode = 10402,
            showUpToDate = true,
        )

        assertTrue(gate is StartupGate.Feedback)
        assertEquals(SystemFeedbackState.UpToDate, (gate as StartupGate.Feedback).state)
    }

    private fun release(
        minimumVersionCode: Int = 0,
        mandatory: Boolean = false,
        maintenance: MaintenanceNotice = MaintenanceNotice(),
    ) = ReleaseManifest(
        versionName = "1.4.1",
        versionCode = 10402,
        minimumVersionCode = minimumVersionCode,
        apkUrl = "https://example.com/stk.apk",
        sha256 = "a".repeat(64),
        mandatory = mandatory,
        notes = emptyList(),
        maintenance = maintenance,
    )
}
