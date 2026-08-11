package com.zzyihao.stk.data.project

import com.zzyihao.stk.data.auth.AuthException
import com.zzyihao.stk.data.session.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class HttpProjectRepository(private val endpoint: String, private val tokenStore: TokenStore, private val cache: ProjectCache) : ProjectRepository {
    override suspend fun listCategories(): List<ProjectCategory> = try {
        val categories = get("categories", emptyMap()).optJSONArray("items").toCategories()
            .filterNot { it.id == "all" }
            .let { listOf(ProjectCategory("all", "全部", 0)) + it }
        cache.saveCategories(categories)
        categories
    } catch (error: Throwable) {
        cache.readCategories() ?: throw error
    }
    override suspend fun listProjects(query: String, categoryId: String?, cursor: String?, refresh: Boolean): ProjectPage {
        val params = buildMap { put("q", query); categoryId?.let { put("category_id", it) }; cursor?.let { put("cursor", it) } }
        return try {
            val page = get("projects", params).toPage()
            if (cursor == null) {
                val key = ProjectCache.pageKey(query, categoryId)
                cache.savePage(key, page)
                page
            } else page
        } catch (error: Throwable) {
            if (cursor == null) cache.readPage(ProjectCache.pageKey(query, categoryId)) ?: throw error
            else throw error
        }
    }
    override suspend fun getProjectDetail(projectId: String): ProjectDetail = try {
        val detail = get("projects/$projectId", emptyMap()).toDetail()
        cache.saveDetail(detail)
        detail
    } catch (error: Throwable) {
        cache.readDetail(projectId) ?: throw error
    }
    override suspend fun recordProjectView(projectId: String) { post("projects/$projectId/view", JSONObject()) }

    private suspend fun get(path: String, params: Map<String, String>): JSONObject = request("GET", path, params, null)
    private suspend fun post(path: String, body: JSONObject): JSONObject = request("POST", path, emptyMap(), body)
    private suspend fun request(method: String, path: String, params: Map<String, String>, body: JSONObject?): JSONObject = withContext(Dispatchers.IO) {
        val query = params.filterValues { it.isNotBlank() }.entries.joinToString("&") { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}" }
        val separator = if (endpoint.contains("?")) "&" else "?"
        val url = URL("$endpoint${separator}resource=$path${if (query.isNotBlank()) "&$query" else ""}")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method; connection.connectTimeout = 10_000; connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json"); connection.setRequestProperty("X-STK-Request-Id", UUID.randomUUID().toString())
            val session = tokenStore.session.first()
            if (session is com.zzyihao.stk.data.session.SessionState.LoggedIn) {
                connection.setRequestProperty("Authorization", "Bearer ${session.session.accessToken}")
            }
            if (method != "GET") { connection.doOutput = true; connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); connection.outputStream.use { it.write(body.toString().toByteArray()) } }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val response = runCatching { JSONObject(responseText) }
                .getOrElse { throw ProjectException(5000, "服务器返回格式错误", cause = it) }
            val code = response.optInt("code", 5000)
            if (code != 0) {
                val message = response.optString("message", "请求失败")
                val requestId = response.optString("request_id").takeIf(String::isNotBlank)
                if (code in setOf(4010, 4011)) {
                    tokenStore.clear()
                    throw AuthException(code, message, requestId)
                }
                throw ProjectException(code, message, requestId)
            }
            response.optJSONObject("data") ?: JSONObject()
        } catch (error: AuthException) {
            throw error
        } catch (error: ProjectException) {
            throw error
        } catch (error: SocketTimeoutException) {
            throw ProjectException(5002, "请求超时，请稍后重试", cause = error)
        } catch (error: UnknownHostException) {
            throw ProjectException(5003, "当前没有网络", cause = error)
        } catch (error: ConnectException) {
            throw ProjectException(5001, "网络连接失败", cause = error)
        } catch (error: Exception) {
            throw ProjectException(5000, "服务暂时异常", cause = error)
        } finally { connection.disconnect() }
    }
}

private fun JSONArray?.toCategories(): List<ProjectCategory> { val array = this ?: return emptyList(); return List(array.length()) { val o = array.getJSONObject(it); ProjectCategory(o.optString("id"), o.optString("name"), o.optInt("sort")) } }
private fun JSONObject.toPage(): ProjectPage { val array = optJSONArray("items") ?: JSONArray(); val next = opt("next_cursor").takeUnless { it == JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }; return ProjectPage(List(array.length()) { projectSummaryFromJson(array.getJSONObject(it)) }, next, emptyText = optString("empty_text", "暂无项目")) }
private fun JSONObject.toDetail(): ProjectDetail = projectDetailFromJson(this)
private fun JSONObject.optionalText(name: String): String =
    opt(name).takeUnless { it == null || it == JSONObject.NULL }?.toString()?.trim().orEmpty()

private fun projectSummaryFromJson(json: JSONObject) = ProjectSummary(
    id = json.optString("id"),
    title = json.optString("title"),
    summary = json.optString("summary"),
    categoryId = json.optString("category_id"),
    categoryName = json.optString("category_name"),
    coverUrl = json.optString("cover_url"),
    publisherName = json.optString("publisher_name"),
    memberLabel = json.optionalText("member_label"),
    publishedAt = normalizeProjectTimestamp(
        publishedAt = json.optionalText("published_at"),
        updatedAt = json.optionalText("updated_at"),
    ),
    viewCount = json.optInt("view_count"),
    publisherAvatarUrl = json.optString("publisher_avatar_url"),
)
private fun projectDetailFromJson(json: JSONObject): ProjectDetail {
    val images = json.optJSONArray("images") ?: JSONArray()
    val contacts = json.optJSONArray("contacts") ?: JSONArray()
    val summary = json.getJSONObject("summary")
    val allowedHostsJson = json.optJSONArray("allowed_external_hosts")
    val allowedHosts = allowedHostsJson?.let { array ->
        (0 until array.length()).mapNotNull { array.optString(it).trim().takeIf(String::isNotBlank) }.toSet()
    }?.takeIf { it.isNotEmpty() } ?: setOf("stk.zz-yihao.com")
    val actionsJson = json.optJSONArray("owner_actions")
    val ownerActions = actionsJson?.let { array ->
        (0 until array.length()).mapNotNull { array.optString(it).trim().takeIf(String::isNotBlank) }.toSet()
    } ?: emptySet()
    return ProjectDetail(
        summary = projectSummaryFromJson(summary),
        images = List(images.length()) { val item = images.getJSONObject(it); ProjectImage(item.optString("id"), item.optString("url"), item.optString("alt")) },
        contacts = List(contacts.length()) { val item = contacts.getJSONObject(it); ProjectContact(item.optString("type"), item.optString("value"), item.optString("label")) },
        publisherUid = json.optLong("publisher_uid"),
        status = summary.optString("status", "published").let { if (it == "unpublished") "offline" else it },
        rejectionReason = summary.optString("rejection_reason"),
        rowVersion = summary.optInt("row_version"),
        allowedExternalHosts = allowedHosts,
        ownerActions = ownerActions,
    )
}
