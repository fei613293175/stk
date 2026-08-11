package com.zzyihao.stk.data.project

enum class MyProjectStatus(val wire: String, val label: String) {
    ALL("", "全部"),
    PENDING("pending", "待审核"),
    PUBLISHED("published", "已发布"),
    REJECTED("rejected", "已驳回"),
    UNPUBLISHED("offline", "已下架"),
    DELETED("deleted", "已删除"),
}

data class ProjectImageDraft(
    val localUri: String,
    val mimeType: String,
    val sizeBytes: Long,
    val displayName: String,
    val sortOrder: Int,
    val isCover: Boolean,
    val uploadProgress: Int = 0,
    val uploadError: String? = null,
    val storageKey: String? = null,
    val assetId: String? = null,
    val uploadRequestId: String = java.util.UUID.randomUUID().toString(),
)

data class ProjectDraft(
    val id: String? = null,
    val title: String = "",
    val summary: String = "",
    val categoryId: String = "",
    val phone: String = "",
    val wechat: String = "",
    val qq: String = "",
    val website: String = "",
    val images: List<ProjectImageDraft> = emptyList(),
    val resubmit: Boolean = false,
    val rowVersion: Int = 0,
    val contactName: String = "",
    val requestId: String = java.util.UUID.randomUUID().toString(),
)

data class MyProject(
    val id: String,
    val title: String,
    val summary: String,
    val categoryId: String,
    val categoryName: String,
    val status: MyProjectStatus,
    val rejectionReason: String = "",
    val updatedAt: String = "",
    val images: List<ProjectImageDraft> = emptyList(),
    val rowVersion: Int = 0,
    val contactName: String = "",
    val phone: String = "",
    val wechat: String = "",
    val qq: String = "",
    val website: String = "",
    val coverUrl: String = "",
    val ownerActions: Set<String> = emptySet(),
)

data class MyProjectPage(
    val items: List<MyProject>,
    val nextCursor: String? = null,
    val hasMore: Boolean = nextCursor != null,
    val statusCounts: Map<MyProjectStatus, Int> = emptyMap(),
    val fromCache: Boolean = false,
)

data class ContactTypeOption(val wire: String, val label: String)

data class PublishingConfig(
    val titleMin: Int = 4,
    val titleMax: Int = 60,
    val summaryMin: Int = 10,
    val summaryMax: Int = 1000,
    val maxImages: Int = 9,
    val maxImageBytes: Long = 10L * 1024L * 1024L,
    val imageLongEdge: Int = 1920,
    val jpegQuality: Int = 82,
    val allowedMimeTypes: Set<String> = ImageUploadProcessor.AllowedMimeTypes,
    val editRequiresReview: Boolean = true,
    val contactTypes: List<ContactTypeOption> = listOf(
        ContactTypeOption("phone", "手机"),
        ContactTypeOption("wechat", "微信"),
        ContactTypeOption("qq", "QQ"),
        ContactTypeOption("url", "网址"),
    ),
)

class ProjectSubmissionException(val code: Int, override val message: String) : Exception(message)
