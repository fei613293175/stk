package com.zzyihao.stk.data.account

import android.content.Context
import com.zzyihao.stk.data.session.SessionState
import com.zzyihao.stk.data.session.TokenStore
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class HttpAccountRepository(
    private val endpoint: String,
    private val tokenStore: TokenStore,
    context: Context,
) : AccountRepository {
    private val cache = context.applicationContext.getSharedPreferences("stk-account-cache", Context.MODE_PRIVATE)

    override suspend fun getProfile(): ProfileCard = load("me/profile", ::parseProfile)

    override suspend fun getMembership(): MembershipCard = load("me/member", ::parseMembership)

    override suspend fun getWallets(): WalletOverview = load("me/wallets", ::parseWallets)

    override suspend fun getProps(): PropsOverview = load("props/display", ::parseProps)

    override suspend fun getSupport(): SupportCard = load("support", ::parseSupport)

    override suspend fun getOverview(): AccountOverview = coroutineScope {
        val profile = async { getProfile() }
        val membership = async { getMembership() }
        val wallets = async { getWallets() }
        val props = async { getProps() }
        val support = async { getSupport() }
        AccountOverview(profile.await(), membership.await(), wallets.await(), props.await(), support.await())
    }

    override fun clearCache() {
        cache.edit().clear().commit()
    }

    private suspend fun <T> load(resource: String, parser: (JSONObject, Boolean) -> T): T = withContext(Dispatchers.IO) {
        val session = tokenStore.session.first() as? SessionState.LoggedIn
            ?: throw AccountException(4010, "请先登录后查看")
        val uid = session.session.user.uid
        try {
            val data = request(resource, session.session.accessToken)
            cache.edit().putString(cacheKey(uid, resource), data.toString()).commit()
            parser(data, false)
        } catch (error: AccountException) {
            if (error.code in setOf(4010, 4011, 4030)) throw error
            readCached(uid, resource, parser) ?: throw error
        } catch (error: Exception) {
            readCached(uid, resource, parser)
                ?: throw AccountException(5001, error.message ?: "无法连接服务器")
        }
    }

    private suspend fun request(resource: String, accessToken: String): JSONObject {
        val separator = if (endpoint.contains("?")) "&" else "?"
        val connection = URL("$endpoint${separator}resource=$resource").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val response = runCatching { JSONObject(body) }
                .getOrElse { throw AccountException(5000, "账户响应格式错误") }
            val code = response.optInt("code", 5000)
            if (code != 0) {
                if (code in setOf(4010, 4011)) tokenStore.clear()
                throw AccountException(
                    code,
                    response.optString("message", "账户信息加载失败"),
                    response.optString("request_id").takeIf(String::isNotBlank),
                )
            }
            return response.optJSONObject("data") ?: throw AccountException(5000, "账户响应缺少数据")
        } finally {
            connection.disconnect()
        }
    }

    private fun <T> readCached(uid: Long, resource: String, parser: (JSONObject, Boolean) -> T): T? = runCatching {
        parser(JSONObject(cache.getString(cacheKey(uid, resource), null) ?: return null), true)
    }.getOrNull()

    private fun cacheKey(uid: Long, resource: String) = "$uid:${resource.replace('/', '_')}"
}

internal fun parseProfile(json: JSONObject, fromCache: Boolean = false) = ProfileCard(
    uid = json.optLong("uid"),
    username = json.optString("username"),
    mobileMasked = json.optString("mobile_masked"),
    memberLabel = json.optString("member_label", "普通用户"),
    bio = json.optString("bio"),
    avatarUrl = json.optString("avatar_url"),
    placeholderMessage = json.optString("placeholder_message", "功能筹备中"),
    fromCache = fromCache,
)

internal fun parseMembership(json: JSONObject, fromCache: Boolean = false): MembershipCard {
    val benefits = json.optJSONArray("benefits") ?: JSONArray()
    return MembershipCard(
        label = json.optString("label", "普通用户"),
        level = json.optString("level", "L1"),
        startsAt = json.optString("starts_at").takeIf(String::isNotBlank),
        expiresAt = json.optString("expires_at").takeIf(String::isNotBlank),
        status = runCatching { MembershipStatus.valueOf(json.optString("status", "inactive").uppercase()) }
            .getOrDefault(MembershipStatus.INACTIVE),
        title = json.optString("title", "商推客会员"),
        openButtonText = json.optString("open_button_text", "立即开通"),
        benefits = List(benefits.length()) { index ->
            val item = benefits.optJSONObject(index) ?: JSONObject()
            BenefitItem(item.optString("title"), item.optString("description"), item.optBoolean("enabled"))
        },
        showCard = json.optBoolean("show_card", true),
        fromCache = fromCache,
    )
}

internal fun parseWallets(json: JSONObject, fromCache: Boolean = false): WalletOverview {
    fun card(key: String, fallbackTitle: String) = (json.optJSONObject(key) ?: JSONObject()).let {
        BalanceCard(
            title = it.optString("title", fallbackTitle),
            amount = it.optString("amount", "0.00"),
            description = it.optString("description", "仅展示账户余额"),
        )
    }
    return WalletOverview(
        commission = card("commission", "佣金账户"),
        tasks = card("tasks", "任务账户"),
        showWallets = json.optBoolean("show_wallets", true),
        fromCache = fromCache,
    )
}

internal fun parseProps(json: JSONObject, fromCache: Boolean = false): PropsOverview {
    val items = json.optJSONArray("items") ?: JSONArray()
    return PropsOverview(
        items = List(items.length()) { index ->
            val item = items.optJSONObject(index) ?: JSONObject()
            PropItem(
                id = item.optString("id", item.optString("prop_id")),
                title = item.optString("title"),
                description = item.optString("description"),
                iconUrl = item.optString("icon_url"),
                sortOrder = item.optInt("sort_order"),
            )
        },
        showCenter = json.optBoolean("show_center", true),
        fromCache = fromCache,
    )
}

internal fun parseSupport(json: JSONObject, fromCache: Boolean = false): SupportCard {
    val hosts = json.optJSONArray("allowed_url_hosts") ?: JSONArray()
    return SupportCard(
        type = json.optString("type", "wechat"),
        label = json.optString("label", "在线客服"),
        value = json.optString("value"),
        serviceHours = json.optString("service_hours", "工作日 09:00-18:00"),
        copyEnabled = json.optBoolean("copy_enabled", true),
        allowedUrlHosts = (0 until hosts.length()).mapNotNull { hosts.optString(it).lowercase().takeIf(String::isNotBlank) }.toSet(),
        fromCache = fromCache,
    )
}
