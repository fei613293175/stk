package com.zzyihao.stk.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRetryDurationTest {
    @Test
    fun formatsSecondsAndMinuteBoundaries() {
        assertEquals("59 秒", formatRetryDuration(59))
        assertEquals("1 分 0 秒", formatRetryDuration(60))
        assertEquals("14 分 32 秒", formatRetryDuration(872))
    }
}
