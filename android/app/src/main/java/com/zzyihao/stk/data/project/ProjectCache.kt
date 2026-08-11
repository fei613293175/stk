package com.zzyihao.stk.data.project

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ProjectCache(context: Context) {
    private val database = ProjectCacheDatabase.get(context)
    private val dao = database.projectCacheDao()

    suspend fun savePage(key: String, page: ProjectPage) {
        val payload = JSONObject().apply {
            put("next_cursor", page.nextCursor ?: JSONObject.NULL)
            put("empty_text", page.emptyText)
            put("items", JSONArray().apply { page.items.forEach { put(it.toJson()) } })
        }
        dao.savePage(ProjectPageCacheEntity(key, payload.toString(), System.currentTimeMillis()))
    }

    suspend fun saveCategories(categories: List<ProjectCategory>) {
        dao.deleteCategories()
        dao.saveCategories(categories.map {
            ProjectCategoryCacheEntity(it.id, it.name, it.sort, System.currentTimeMillis())
        })
    }

    suspend fun readCategories(): List<ProjectCategory>? = runCatching {
        dao.readCategories().takeIf { it.isNotEmpty() }?.map {
            ProjectCategory(it.categoryId, it.name, it.sortOrder)
        }
    }.getOrNull()

    suspend fun readPage(key: String): ProjectPage? = runCatching {
        val payload = dao.readPage(key) ?: return null
        val json = JSONObject(payload)
        val items = json.getJSONArray("items")
            .let { array -> List(array.length()) { index -> projectSummaryFromJson(array.getJSONObject(index)) } }
        val next = json.opt("next_cursor").takeUnless { it == JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }
        ProjectPage(items, next, true, json.optString("empty_text", "暂无项目"))
    }.getOrNull()

    suspend fun saveDetail(detail: ProjectDetail) {
        dao.saveDetail(ProjectDetailCacheEntity(detail.summary.id, detail.toJson().toString(), System.currentTimeMillis()))
    }

    suspend fun readDetail(id: String): ProjectDetail? = runCatching {
        dao.readDetail(id)?.let { projectDetailFromJson(JSONObject(it)).copy(fromCache = true) }
    }.getOrNull()

    fun clear() {
        database.queryExecutor.execute { database.clearAllTables() }
    }

    companion object {
        fun pageKey(query: String, categoryId: String?): String =
            (query.trim().lowercase() + "|" + categoryId.orEmpty()).hashCode().toUInt().toString(16)
    }
}

private fun ProjectSummary.toJson() = JSONObject().apply {
    put("id", id); put("title", title); put("summary", summary); put("category_id", categoryId)
    put("category_name", categoryName); put("cover_url", coverUrl); put("publisher_name", publisherName)
    put("publisher_avatar_url", publisherAvatarUrl); put("member_label", memberLabel)
    put("published_at", publishedAt); put("view_count", viewCount)
}

private fun projectSummaryFromJson(json: JSONObject): ProjectSummary = ProjectSummary(
    id = json.optString("id"), title = json.optString("title"), summary = json.optString("summary"),
    categoryId = json.optString("category_id"), categoryName = json.optString("category_name"),
    coverUrl = json.optString("cover_url"), publisherName = json.optString("publisher_name"),
    publisherAvatarUrl = json.optString("publisher_avatar_url"), memberLabel = json.optString("member_label"),
    publishedAt = json.optString("published_at"), viewCount = json.optInt("view_count"),
)

private fun ProjectDetail.toJson() = JSONObject().apply {
    put("summary", summary.toJson().apply { put("status", status); put("rejection_reason", rejectionReason); put("row_version", rowVersion) }); put("publisher_uid", publisherUid)
    put("images", JSONArray().apply { images.forEach { put(JSONObject().apply { put("id", it.id); put("url", it.url); put("alt", it.alt) }) } })
    put("contacts", JSONArray().apply { contacts.forEach { put(JSONObject().apply { put("type", it.type); put("value", it.value); put("label", it.label) }) } })
    put("allowed_external_hosts", JSONArray().apply { allowedExternalHosts.forEach { put(it) } })
}

private fun projectDetailFromJson(json: JSONObject): ProjectDetail {
    val imagesJson = json.optJSONArray("images") ?: JSONArray()
    val contactsJson = json.optJSONArray("contacts") ?: JSONArray()
    val summary = json.getJSONObject("summary")
    val allowedHostsJson = json.optJSONArray("allowed_external_hosts")
    val allowedHosts = allowedHostsJson?.let { array ->
        (0 until array.length()).mapNotNull { array.optString(it).trim().takeIf(String::isNotBlank) }.toSet()
    }?.takeIf { it.isNotEmpty() } ?: setOf("stk.zz-yihao.com")
    return ProjectDetail(
        summary = projectSummaryFromJson(summary),
        images = List(imagesJson.length()) { val item = imagesJson.getJSONObject(it); ProjectImage(item.optString("id"), item.optString("url"), item.optString("alt")) },
        contacts = List(contactsJson.length()) { val item = contactsJson.getJSONObject(it); ProjectContact(item.optString("type"), item.optString("value"), item.optString("label")) },
        publisherUid = json.optLong("publisher_uid"),
        status = summary.optString("status", "published").let { if (it == "unpublished") "offline" else it },
        rejectionReason = summary.optString("rejection_reason"),
        rowVersion = summary.optInt("row_version"),
        allowedExternalHosts = allowedHosts,
    )
}
