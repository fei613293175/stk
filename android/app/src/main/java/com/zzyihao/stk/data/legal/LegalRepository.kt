package com.zzyihao.stk.data.legal

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class LegalDocumentContent(
    val documentId: String,
    val title: String,
    val version: String,
    val content: String,
    val fromServer: Boolean,
    val effectiveDate: String? = null,
    val cached: Boolean = false,
)

interface LegalRepository {
    suspend fun getDocument(documentId: String): LegalDocumentContent
    fun clearCache() = Unit
}

class LegalException(
    override val message: String,
    val requestId: String? = null,
) : Exception(message)

class FakeLegalRepository : LegalRepository {
    override suspend fun getDocument(documentId: String): LegalDocumentContent = legalFallback(documentId, false)
}

class HttpLegalRepository(
    private val endpoint: String,
    context: Context,
) : LegalRepository {
    private val cache = context.applicationContext.getSharedPreferences("stk-legal-cache", Context.MODE_PRIVATE)

    override suspend fun getDocument(documentId: String): LegalDocumentContent = withContext(Dispatchers.IO) {
        val separator = if (endpoint.contains('?')) "&" else "?"
        val connection = URL(endpoint + separator + "action=legal_document").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("X-STK-Request-Id", UUID.randomUUID().toString())
            connection.outputStream.use { output ->
                output.write(JSONObject().put("document_id", documentId).toString().toByteArray(Charsets.UTF_8))
            }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = JSONObject(stream?.bufferedReader()?.use { it.readText() }.orEmpty())
            if (response.optInt("code", 5000) != 0) {
                throw LegalException(
                    response.optString("message", "协议加载失败"),
                    response.optString("request_id").takeIf(String::isNotBlank),
                )
            }
            val data = response.optJSONObject("data") ?: throw LegalException("协议响应格式错误")
            val content = data.optString("content").trim()
            if (content.isBlank()) throw LegalException("协议内容为空")
            val document = LegalDocumentContent(
                documentId = data.optString("document_id", documentId),
                title = data.optString("title").ifBlank { legalFallback(documentId, false).title },
                version = data.optString("version").ifBlank { "未标注" },
                content = content,
                fromServer = true,
                effectiveDate = data.optString("effective_date").takeIf(String::isNotBlank),
            )
            saveCached(document)
            document
        } catch (error: Exception) {
            readCached(documentId) ?: when (error) {
                is LegalException -> throw error
                else -> throw LegalException(error.message ?: "协议加载失败")
            }
        } finally {
            connection.disconnect()
        }
    }

    override fun clearCache() {
        cache.edit().clear().apply()
    }

    private fun saveCached(document: LegalDocumentContent) {
        val payload = JSONObject()
            .put("document_id", document.documentId)
            .put("title", document.title)
            .put("version", document.version)
            .put("content", document.content)
            .put("effective_date", document.effectiveDate)
        cache.edit().putString(document.documentId, payload.toString()).apply()
    }

    private fun readCached(documentId: String): LegalDocumentContent? = runCatching {
        val payload = JSONObject(cache.getString(documentId, null) ?: return null)
        val content = payload.optString("content").trim()
        if (content.isBlank()) return null
        LegalDocumentContent(
            documentId = payload.optString("document_id", documentId),
            title = payload.optString("title").ifBlank { legalFallback(documentId, false).title },
            version = payload.optString("version").ifBlank { "未标注" },
            content = content,
            fromServer = false,
            effectiveDate = payload.optString("effective_date").takeIf(String::isNotBlank),
            cached = true,
        )
    }.getOrNull()
}

fun legalFallback(documentId: String, fromServer: Boolean): LegalDocumentContent = when (documentId) {
    "privacy_policy" -> LegalDocumentContent(
        documentId = documentId,
        title = "隐私政策",
        version = "1.4.0（内置说明）",
        content = "商推客仅在提供登录、项目发布和安全验证服务所必需的范围内处理账户、设备和业务数据。\n\n手机号、登录令牌及验证码用于身份认证与安全防护；项目图片及联系方式仅在用户主动提交后按服务规则处理。\n\n客户端不会将非白名单外部链接直接作为可执行操作。你可随时退出登录，客户端会清理本地敏感会话数据。",
        fromServer = fromServer,
    )
    else -> LegalDocumentContent(
        documentId = "user_agreement",
        title = "用户协议",
        version = "1.4.0（内置说明）",
        content = "欢迎使用商推客。\n\n商推客为用户提供推广项目的浏览、发布及相关服务。用户应保证提交内容真实、合法，不得发布违法、侵权、欺诈或误导性信息。\n\n平台可依据服务规则对项目进行审核、下架或限制访问。项目展示不构成任何交易、收益或合作承诺。",
        fromServer = fromServer,
    )
}
