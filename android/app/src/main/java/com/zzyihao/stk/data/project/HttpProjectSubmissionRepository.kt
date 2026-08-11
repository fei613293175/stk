package com.zzyihao.stk.data.project

import android.content.Context
import android.net.Uri
import com.zzyihao.stk.data.session.SessionState
import com.zzyihao.stk.data.session.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.FileInputStream
import java.util.UUID

class HttpProjectSubmissionRepository(
    private val endpoint: String,
    private val tokenStore: TokenStore,
    context: Context,
) : ProjectSubmissionRepository {
    private val contentResolver = context.applicationContext.contentResolver
    private val mineCache = context.applicationContext.getSharedPreferences("stk-my-project-cache", Context.MODE_PRIVATE)
    override suspend fun getPublishingConfig(): PublishingConfig {
        val data = request("GET", "publishing/config", emptyMap(), null)
        val contacts = data.optJSONArray("contact_types") ?: JSONArray()
        val mimeTypes = data.optJSONArray("allowed_image_types") ?: JSONArray()
        return PublishingConfig(
            titleMin = data.optInt("title_min", 4).coerceAtLeast(1),
            titleMax = data.optInt("title_max", 60).coerceAtLeast(4),
            summaryMin = data.optInt("summary_min", 10).coerceAtLeast(1),
            summaryMax = data.optInt("summary_max", 1000).coerceAtLeast(10),
            maxImages = data.optInt("max_images", 9).coerceIn(1, 12),
            maxImageBytes = data.optLong("max_image_bytes", ImageUploadProcessor.MaxImageBytes).coerceAtLeast(1024),
            imageLongEdge = data.optInt("image_long_edge", 1920).coerceIn(640, 4096),
            jpegQuality = data.optInt("jpeg_quality", 82).coerceIn(50, 95),
            allowedMimeTypes = (0 until mimeTypes.length()).mapNotNull { mimeTypes.optString(it).takeIf(String::isNotBlank) }.toSet().ifEmpty { ImageUploadProcessor.AllowedMimeTypes },
            editRequiresReview = data.optBoolean("edit_requires_review", true),
            contactTypes = (0 until contacts.length()).mapNotNull { index ->
                contacts.optJSONObject(index)?.let { item -> ContactTypeOption(item.optString("wire"), item.optString("label")) }.takeIf { it?.wire?.isNotBlank() == true }
            },
        )
    }
    override suspend fun uploadImage(image: ProjectImageDraft, onProgress: (Int) -> Unit): ProjectImageDraft {
        onProgress(5)
        val uploaded = uploadTemporaryImage(image)
        onProgress(100)
        return image.copy(
            mimeType = uploaded.mimeType,
            sizeBytes = uploaded.sizeBytes,
            storageKey = uploaded.storageKey,
            assetId = uploaded.assetId,
            uploadProgress = 100,
            uploadError = null,
        )
    }
    override suspend fun deleteUploadedImage(image: ProjectImageDraft) {
        val storageKey = image.storageKey ?: return
        if (image.localUri.startsWith("http://") || image.localUri.startsWith("https://")) return
        image.assetId?.toLongOrNull()?.let { assetId ->
            request("DELETE", "uploads/project-images/$assetId", emptyMap(), null, image.uploadRequestId)
        } ?: request("POST", "upload/delete", emptyMap(), JSONObject().put("storage_key", storageKey), UUID.randomUUID().toString())
    }
    override suspend fun listMyProjects(status: MyProjectStatus, cursor: String?): MyProjectPage = try {
        request("GET", "me/projects", buildMap { if (status != MyProjectStatus.ALL) put("status", status.wire); cursor?.let { put("cursor", it) } }, null)
            .toMyPage()
            .also { page -> if (cursor == null) saveMyProjectsPage(status, page) }
    } catch (error: ProjectSubmissionException) {
        if (cursor == null && error.code in setOf(5001, 5002)) loadMyProjectsPage(status) ?: throw error
        else throw error
    }
    override suspend fun getMyProject(id: String): MyProject {
        val data = request("GET", "mine/$id", emptyMap(), null)
        val projectJson = data.optJSONObject("project")
        val contacts = data.optJSONArray("contacts") ?: JSONArray()
        val project = projectJson?.toMyProject()?.copy(
            contactName = projectJson.optString("contact_name"),
            phone = contacts.findContact("phone"),
            wechat = contacts.findContact("wechat"),
            qq = contacts.findContact("qq"),
            website = contacts.findContact("url"),
        )
            ?: throw ProjectSubmissionException(4040, "项目不存在")
        return project.copy(images = data.optJSONArray("images").toProjectImages())
    }
    override suspend fun createProject(draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject = submit("POST", "projects", draft, onProgress)
    override suspend fun updateProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject = submit("PUT", "projects/$id", draft, onProgress)
    override suspend fun resubmitProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject = submit("POST", "projects/$id/resubmit", draft, onProgress)
    override suspend fun unpublishProject(id: String): MyProject {
        val version = runCatching { getMyProject(id).rowVersion }.getOrDefault(0)
        return request("POST", "projects/$id/offline", emptyMap(), JSONObject().put("version", version).put("row_version", version), UUID.randomUUID().toString()).optJSONObject("project")?.toMyProject()
            ?: throw ProjectSubmissionException(5000, "下架响应格式错误")
    }
    override suspend fun softDeleteProject(id: String) {
        val version = runCatching { getMyProject(id).rowVersion }.getOrDefault(0)
        request("DELETE", "projects/$id", emptyMap(), JSONObject().put("version", version).put("row_version", version), UUID.randomUUID().toString())
    }

    private suspend fun submit(method: String, resource: String, draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject {
        onProgress(5)
        try {
            val uploaded = draft.images.mapIndexed { index, image ->
                val result = image.storageKey?.let { UploadedImage(image.assetId, it, image.mimeType, image.sizeBytes) }
                    ?: uploadTemporaryImage(image)
                result.also { onProgress(10 + ((index + 1) * 55 / draft.images.size.coerceAtLeast(1))) }
            }
            val images = JSONArray().apply {
                uploaded.forEachIndexed { index, image ->
                    put(JSONObject().apply {
                        put("storage_key", image.storageKey)
                        image.assetId?.let { put("asset_id", it) }
                        put("mime_type", image.mimeType)
                        put("size_bytes", image.sizeBytes)
                        put("sort_order", index)
                        put("is_cover", draft.images.getOrNull(index)?.isCover == true)
                    })
                }
            }
            val body = JSONObject().apply {
                put("title", draft.title); put("summary", draft.summary); put("category_id", draft.categoryId); put("contact_name", draft.contactName)
                put("contacts", JSONArray().apply {
                    if (draft.phone.isNotBlank()) put(JSONObject().put("type", "phone").put("value", draft.phone).put("label", "电话"))
                    if (draft.wechat.isNotBlank()) put(JSONObject().put("type", "wechat").put("value", draft.wechat).put("label", "微信"))
                    if (draft.qq.isNotBlank()) put(JSONObject().put("type", "qq").put("value", draft.qq).put("label", "QQ"))
                    if (draft.website.isNotBlank()) put(JSONObject().put("type", "url").put("value", draft.website).put("label", "项目网址"))
                }); put("images", images)
                put("row_version", draft.rowVersion)
            }
            onProgress(70)
            return (request(method, resource, emptyMap(), body, draft.requestId).optJSONObject("project")?.toMyProject()
                ?: throw ProjectSubmissionException(5000, "发布响应格式错误")).also { onProgress(100) }
        } catch (error: Throwable) { throw error }
    }

    private suspend fun uploadTemporaryImage(image: ProjectImageDraft): UploadedImage = withContext(Dispatchers.IO) {
        val boundary = "----STK${UUID.randomUUID()}"
        val separator = if (endpoint.contains("?")) "&" else "?"
        val connection = URL("$endpoint${separator}resource=uploads/project-images").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 60_000
            connection.doOutput = true
            connection.setChunkedStreamingMode(64 * 1024)
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            val requestId = image.uploadRequestId
            connection.setRequestProperty("X-STK-Request-Id", requestId)
            connection.setRequestProperty("Idempotency-Key", requestId)
            val state = tokenStore.session.first()
            if (state is SessionState.LoggedIn) connection.setRequestProperty("Authorization", "Bearer ${state.session.accessToken}")
            connection.outputStream.buffered().use { output ->
                output.write("--$boundary\r\n".toByteArray())
                output.write("Content-Disposition: form-data; name=\"image\"; filename=\"project-image\"\r\n".toByteArray())
                output.write("Content-Type: ${image.mimeType}\r\n\r\n".toByteArray())
                val imageUri = Uri.parse(image.localUri)
                val inputStream = if (imageUri.scheme == "file") imageUri.path?.let(::FileInputStream) else contentResolver.openInputStream(imageUri)
                inputStream?.use { input -> input.copyTo(output, 64 * 1024) }
                    ?: throw ProjectSubmissionException(4001, "无法读取所选图片")
                output.write("\r\n--$boundary--\r\n".toByteArray())
            }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = JSONObject(stream?.bufferedReader()?.use { it.readText() }.orEmpty())
            val code = response.optInt("code", 5000)
            if (code != 0) {
                if (code in setOf(4010, 4011)) tokenStore.clear()
                throw ProjectSubmissionException(code, response.optString("message", "图片上传失败"))
            }
            val data = response.optJSONObject("data") ?: throw ProjectSubmissionException(5000, "图片上传响应格式错误")
            UploadedImage(
                assetId = data.optString("asset_id").takeIf { it.isNotBlank() },
                storageKey = data.getString("storage_key"),
                mimeType = data.getString("mime_type"),
                sizeBytes = data.getLong("size_bytes"),
            )
        } catch (error: ProjectSubmissionException) {
            throw error
        } catch (error: Exception) {
            throw ProjectSubmissionException(5001, error.message ?: "图片上传失败")
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun request(method: String, resource: String, params: Map<String, String>, body: JSONObject?, idempotencyKey: String? = null): JSONObject = withContext(Dispatchers.IO) {
        val query = params.entries.joinToString("&") { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}" }
        val separator = if (endpoint.contains("?")) "&" else "?"
        val connection = URL("$endpoint${separator}resource=$resource${if (query.isNotBlank()) "&$query" else ""}").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method; connection.connectTimeout = 10_000; connection.readTimeout = 30_000
            val requestId = idempotencyKey ?: UUID.randomUUID().toString()
            connection.setRequestProperty("Accept", "application/json"); connection.setRequestProperty("X-STK-Request-Id", requestId)
            if (method != "GET") connection.setRequestProperty("Idempotency-Key", requestId)
            val state = tokenStore.session.first()
            if (state is SessionState.LoggedIn) connection.setRequestProperty("Authorization", "Bearer ${state.session.accessToken}")
            if (body != null) { connection.doOutput = true; connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); connection.outputStream.use { it.write(body.toString().toByteArray()) } }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = JSONObject(stream?.bufferedReader()?.use { it.readText() }.orEmpty())
            val code = response.optInt("code", 5000)
            if (code != 0) {
                if (code in setOf(4010, 4011)) tokenStore.clear()
                throw ProjectSubmissionException(code, response.optString("message", "请求失败"))
            }
            response.optJSONObject("data") ?: JSONObject()
        } catch (e: ProjectSubmissionException) { throw e } catch (e: Exception) { throw ProjectSubmissionException(5001, e.message ?: "无法连接服务器") } finally { connection.disconnect() }
    }

    private suspend fun saveMyProjectsPage(status: MyProjectStatus, page: MyProjectPage) {
        mineCache.edit().putString(myProjectsCacheKey(status), page.toJson().toString()).apply()
    }

    private suspend fun loadMyProjectsPage(status: MyProjectStatus): MyProjectPage? {
        val key = myProjectsCacheKey(status)
        return runCatching {
        mineCache.getString(key, null)
            ?.let(::JSONObject)
            ?.toMyPage(fromCache = true)
        }.getOrNull()
    }

    private suspend fun myProjectsCacheKey(status: MyProjectStatus): String {
        val uid = (tokenStore.session.first() as? SessionState.LoggedIn)?.session?.user?.uid ?: 0L
        return "$uid:${status.wire.ifBlank { "all" }}"
    }
}

private data class UploadedImage(val assetId: String?, val storageKey: String, val mimeType: String, val sizeBytes: Long)

private fun JSONObject.toMyPage(fromCache: Boolean = false): MyProjectPage {
    val array = optJSONArray("items") ?: JSONArray()
    val countsJson = optJSONObject("status_counts")
    return MyProjectPage(
        List(array.length()) { array.getJSONObject(it).toMyProject() },
        opt("next_cursor").takeUnless { it == JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() },
        optBoolean("has_more", false),
        MyProjectStatus.entries.associateWith { countsJson?.optInt(it.wire, 0) ?: 0 },
        fromCache,
    )
}

private fun MyProjectPage.toJson(): JSONObject = JSONObject().apply {
    put("items", JSONArray().apply { this@toJson.items.forEach { put(it.toJson()) } })
    put("next_cursor", nextCursor ?: JSONObject.NULL)
    put("has_more", hasMore)
    put("status_counts", JSONObject().apply {
        this@toJson.statusCounts.forEach { (status, count) -> put(status.wire, count) }
    })
}

private fun MyProject.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("title", title); put("summary", summary)
    put("category_id", categoryId); put("category_name", categoryName); put("status", status.wire)
    put("rejection_reason", rejectionReason); put("updated_at", updatedAt); put("row_version", rowVersion)
    put("contact_name", contactName); put("cover_url", coverUrl)
    put("owner_actions", JSONArray().apply { ownerActions.forEach(::put) })
}
private fun JSONObject.toMyProject(): MyProject {
    val wireStatus = optString("status").let { if (it == "unpublished") "offline" else it }
    val status = MyProjectStatus.entries.firstOrNull { it.wire == wireStatus } ?: MyProjectStatus.PENDING
    val actions = optJSONArray("owner_actions")?.let { values ->
        (0 until values.length()).mapNotNull { values.optString(it).takeIf(String::isNotBlank) }.toSet()
    } ?: emptySet()
    return MyProject(optString("id"), optString("title"), optString("summary"), optString("category_id"), optString("category_name"), status, optString("rejection_reason"), optString("updated_at"), rowVersion = optInt("row_version", 0), contactName = optString("contact_name"), coverUrl = optString("cover_url"), ownerActions = actions)
}

private fun JSONArray.findContact(type: String): String =
    (0 until length()).firstNotNullOfOrNull { index -> getJSONObject(index).takeIf { it.optString("type") == type }?.optString("value") }.orEmpty()

private fun JSONArray?.toProjectImages(): List<ProjectImageDraft> {
    if (this == null) return emptyList()
    return List(length()) { index ->
        val image = getJSONObject(index)
        ProjectImageDraft(
            localUri = image.optString("url"),
            mimeType = image.optString("mime_type", "image/jpeg"),
            sizeBytes = image.optLong("size_bytes", 0),
            displayName = image.optString("alt", "项目图片"),
            sortOrder = image.optInt("sort_order", index),
            isCover = false,
            uploadProgress = 100,
            storageKey = image.optString("storage_key").takeIf { it.isNotBlank() },
            assetId = image.optString("asset_id").takeIf { it.isNotBlank() },
        )
    }.sortedBy { it.sortOrder }.mapIndexed { index, image ->
        image.copy(sortOrder = index, isCover = index == 0)
    }
}
