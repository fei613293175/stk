package com.zzyihao.stk

import android.content.Context

data class StkSession(val accessToken: String, val refreshToken: String, val refreshFamily: String)

object StkSessionStore {
    private const val PREFS = "stk_session"
    private const val ACCESS = "access_token"
    private const val REFRESH = "refresh_token"
    private const val FAMILY = "refresh_family"

    fun read(context: Context): StkSession? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val access = prefs.getString(ACCESS, null) ?: return null
        val refresh = prefs.getString(REFRESH, null) ?: return null
        return StkSession(access, refresh, prefs.getString(FAMILY, "") ?: "")
    }

    fun save(context: Context, session: StkSession) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ACCESS, session.accessToken)
            .putString(REFRESH, session.refreshToken)
            .putString(FAMILY, session.refreshFamily)
            .apply()
    }

    fun clear(context: Context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply() }
}
