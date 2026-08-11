package com.zzyihao.stk.data.account

import com.zzyihao.stk.data.session.SessionState
import com.zzyihao.stk.data.session.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HttpAccountRepository(private val endpoint: String, private val tokenStore: TokenStore) : AccountRepository {
    override suspend fun getOverview(): AccountOverview = withContext(Dispatchers.IO) {
        val separator = if (endpoint.contains("?")) "&" else "?"
        val connection = URL("$endpoint${separator}action=account_overview").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            val state = tokenStore.session.first()
            if (state is SessionState.LoggedIn) connection.setRequestProperty("Authorization", "Bearer ${state.session.accessToken}")
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = JSONObject(stream?.bufferedReader()?.use { it.readText() }.orEmpty())
            val code = response.optInt("code", 5000)
            if (code != 0) {
                if (code in setOf(4010, 4011)) tokenStore.clear()
                throw AccountException(code, response.optString("message", "账户加载失败"))
            }
            response.optJSONObject("data")?.toOverview() ?: throw AccountException(5000, "账户响应格式错误")
        } catch (error: AccountException) {
            throw error
        } catch (error: Exception) {
            throw AccountException(5001, error.message ?: "无法连接服务器")
        } finally {
            connection.disconnect()
        }
    }
}

private fun JSONObject.toOverview(): AccountOverview {
    val p = optJSONObject("profile") ?: JSONObject()
    val m = optJSONObject("membership") ?: JSONObject()
    val c = optJSONObject("commission") ?: JSONObject()
    val t = optJSONObject("tasks") ?: JSONObject()
    val benefits = optJSONArray("benefits") ?: JSONArray()
    val props = optJSONArray("props") ?: JSONArray()
    val support = optJSONObject("support") ?: JSONObject()
    val display = optJSONObject("display") ?: JSONObject()
    return AccountOverview(
        ProfileCard(p.optString("username"), p.optString("mobile_masked"), p.optString("member_label"), p.optString("bio")),
        MembershipCard(
            m.optString("label"),
            m.optString("level"),
            m.optString("expires_at").takeIf { it.isNotBlank() },
            m.optInt("progress"),
            runCatching { MembershipStatus.valueOf(m.optString("status", "inactive").uppercase()) }.getOrDefault(MembershipStatus.INACTIVE),
        ),
        BalanceCard(c.optString("title", "佣金账户"), c.optString("amount"), c.optString("description")),
        BalanceCard(t.optString("title", "任务账户"), t.optString("amount"), t.optString("description")),
        List(benefits.length()) { val item = benefits.getJSONObject(it); BenefitItem(item.optString("title"), item.optString("description"), item.optBoolean("enabled")) },
        List(props.length()) { val item = props.getJSONObject(it); PropItem(item.optString("title"), item.optString("description"), item.optInt("quantity")) },
        SupportCard(
            type = support.optString("type", "wechat"),
            label = support.optString("label", "在线客服"),
            value = support.optString("value"),
            serviceHours = support.optString("service_hours", "工作日 09:00-18:00"),
            copyEnabled = support.optBoolean("copy_enabled", true),
        ),
        AccountDisplayConfig(
            memberTitle = display.optString("member_title", "商推客会员"),
            commissionLabel = display.optString("commission_label", "佣金账户"),
            taskLabel = display.optString("task_label", "任务账户"),
            placeholderMessage = display.optString("placeholder_message", "功能筹备中"),
            showWallets = display.optBoolean("show_wallets", true),
            showMemberCard = display.optBoolean("show_member_card", true),
            showPropsCenter = display.optBoolean("show_props_center", true),
            memberOpenButtonText = display.optString("member_open_button_text", "了解会员"),
        ),
    )
}
