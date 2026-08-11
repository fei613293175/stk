package com.zzyihao.stk.data.session

import com.zzyihao.stk.data.auth.UserSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenStoreStateTest {
    private val user = UserSummary(10001, "测试用户", "138****8000", "普通用户")

    @Test
    fun expiredAccessTokenWithRefreshTokenRequestsRotation() {
        val state = resolveSessionState("access", "refresh", 100, user, 101)

        assertTrue(state is SessionState.NeedsRefresh)
        assertEquals("refresh", (state as SessionState.NeedsRefresh).refreshToken)
    }

    @Test
    fun missingRefreshTokenLogsOut() {
        assertTrue(resolveSessionState("access", "", 200, user, 100) is SessionState.LoggedOut)
    }

    @Test
    fun unexpiredTokensKeepTheSession() {
        val state = resolveSessionState("access", "refresh", 200, user, 100)

        assertTrue(state is SessionState.LoggedIn)
        assertEquals(200, (state as SessionState.LoggedIn).session.expiresAt)
    }

    @Test
    fun refreshIsScheduledThirtySecondsBeforeExpiry() {
        assertEquals(70_000L, refreshDelayMillis(200, 100_000L))
        assertEquals(0L, refreshDelayMillis(100, 100_000L))
    }
}
