package com.zzyihao.stk.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.data.auth.UserSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.stkSessionDataStore by preferencesDataStore(name = "stk_session")

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class NeedsRefresh(val refreshToken: String, val user: UserSummary) : SessionState
    data class LoggedIn(val session: AuthSession) : SessionState
}

class TokenStore(
    private val context: Context,
) {
    private object Keys {
        val AccessToken = stringPreferencesKey("access_token")
        val RefreshToken = stringPreferencesKey("refresh_token")
        val ExpiresAt = longPreferencesKey("expires_at")
        val Uid = longPreferencesKey("uid")
        val Username = stringPreferencesKey("username")
        val MobileMasked = stringPreferencesKey("mobile_masked")
        val MemberLabel = stringPreferencesKey("member_label")
        val PendingRevocationAt = longPreferencesKey("pending_revocation_at")
    }

    val session: Flow<SessionState> = context.stkSessionDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences ->
            val accessToken = preferences[Keys.AccessToken]
            val refreshToken = preferences[Keys.RefreshToken].orEmpty()
            val expiresAt = preferences[Keys.ExpiresAt] ?: 0L
            val nowSeconds = System.currentTimeMillis() / 1000L
            val user = UserSummary(
                uid = preferences[Keys.Uid] ?: 0L,
                username = preferences[Keys.Username].orEmpty(),
                mobileMasked = preferences[Keys.MobileMasked].orEmpty(),
                memberLabel = preferences[Keys.MemberLabel] ?: "普通用户",
            )
            resolveSessionState(accessToken, refreshToken, expiresAt, user, nowSeconds)
        }

    suspend fun save(session: AuthSession) {
        context.stkSessionDataStore.edit { preferences ->
            preferences[Keys.AccessToken] = session.accessToken
            preferences[Keys.RefreshToken] = session.refreshToken
            preferences[Keys.ExpiresAt] = session.expiresAt
            preferences[Keys.Uid] = session.user.uid
            preferences[Keys.Username] = session.user.username
            preferences[Keys.MobileMasked] = session.user.mobileMasked
            preferences[Keys.MemberLabel] = session.user.memberLabel
        }
    }

    suspend fun clear() {
        context.stkSessionDataStore.edit { preferences ->
            preferences.remove(Keys.AccessToken)
            preferences.remove(Keys.RefreshToken)
            preferences.remove(Keys.ExpiresAt)
            preferences.remove(Keys.Uid)
            preferences.remove(Keys.Username)
            preferences.remove(Keys.MobileMasked)
            preferences.remove(Keys.MemberLabel)
        }
    }

    suspend fun markRevocationPending() {
        context.stkSessionDataStore.edit { preferences ->
            preferences[Keys.PendingRevocationAt] = System.currentTimeMillis()
        }
    }

    suspend fun clearPendingRevocation() {
        context.stkSessionDataStore.edit { preferences ->
            preferences.remove(Keys.PendingRevocationAt)
        }
    }
}

internal fun resolveSessionState(
    accessToken: String?,
    refreshToken: String,
    expiresAt: Long,
    user: UserSummary,
    nowSeconds: Long,
): SessionState = when {
    accessToken.isNullOrBlank() || refreshToken.isBlank() -> SessionState.LoggedOut
    expiresAt <= nowSeconds -> SessionState.NeedsRefresh(refreshToken, user)
    else -> SessionState.LoggedIn(AuthSession(accessToken, refreshToken, expiresAt, user))
}

internal fun refreshDelayMillis(expiresAtSeconds: Long, nowMillis: Long): Long =
    ((expiresAtSeconds * 1000L) - nowMillis - 30_000L).coerceAtLeast(0L)
