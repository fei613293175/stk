package com.zzyihao.stk.data.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthBootstrapRouteTest {
    @Test
    fun supportedRoutesArePreserved() {
        listOf("stk://home", "stk://publish", "stk://me", "stk://me/projects").forEach { route ->
            assertEquals(route, normalizeAuthSuccessRoute(route))
        }
    }

    @Test
    fun routeIsNormalizedAndUnknownRoutesFallBackToHome() {
        assertEquals("stk://me", normalizeAuthSuccessRoute(" STK://ME "))
        assertEquals("stk://home", normalizeAuthSuccessRoute("https://example.com"))
        assertEquals("stk://home", normalizeAuthSuccessRoute("stk://project/1"))
    }
}
