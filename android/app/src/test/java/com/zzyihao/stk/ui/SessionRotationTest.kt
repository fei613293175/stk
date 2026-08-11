package com.zzyihao.stk.ui

import com.zzyihao.stk.data.auth.AuthException
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRotationTest {
    @Test
    fun transientFailuresBackOffAndEventuallySucceed() = runBlocking {
        var attempts = 0
        var cleared = false
        val delays = mutableListOf<Long>()

        val result = rotateSession(
            refreshToken = "refresh",
            maxAttempts = 4,
            refresh = {
                attempts += 1
                if (attempts < 3) throw IOException("offline")
            },
            clear = { cleared = true },
            pause = { delays += it },
        )

        assertTrue(result)
        assertEquals(3, attempts)
        assertEquals(listOf(2_000L, 4_000L), delays)
        assertFalse(cleared)
    }

    @Test
    fun invalidRefreshTokenClearsSessionWithoutRetry() = runBlocking {
        var attempts = 0
        var clearCount = 0

        val result = rotateSession(
            refreshToken = "invalid",
            maxAttempts = 4,
            refresh = {
                attempts += 1
                throw AuthException(4011, "invalid")
            },
            clear = { clearCount += 1 },
            pause = { error("must not delay") },
        )

        assertTrue(result)
        assertEquals(1, attempts)
        assertEquals(1, clearCount)
    }

    @Test
    fun exhaustedTransientRetriesKeepStoredSession() = runBlocking {
        var clearCount = 0
        val delays = mutableListOf<Long>()

        val result = rotateSession(
            refreshToken = "refresh",
            maxAttempts = 3,
            refresh = { throw IOException("offline") },
            clear = { clearCount += 1 },
            pause = { delays += it },
        )

        assertFalse(result)
        assertEquals(0, clearCount)
        assertEquals(listOf(2_000L, 4_000L), delays)
    }
}
