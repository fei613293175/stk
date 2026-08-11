package com.zzyihao.stk.data.project

data class ProjectCategory(val id: String, val name: String, val sort: Int = 0)

data class ProjectImage(val id: String, val url: String, val alt: String = "项目图片")

data class ProjectContact(val type: String, val value: String, val label: String)

data class ProjectSummary(
    val id: String,
    val title: String,
    val summary: String,
    val categoryId: String,
    val categoryName: String,
    val coverUrl: String,
    val publisherName: String,
    val memberLabel: String,
    val publishedAt: String,
    val viewCount: Int,
    val publisherAvatarUrl: String = "",
)

internal fun normalizeProjectTimestamp(publishedAt: String?, updatedAt: String?): String =
    sequenceOf(publishedAt, updatedAt)
        .mapNotNull { value -> value?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) } }
        .firstOrNull()
        .orEmpty()

fun ProjectSummary.publisherMeta(publisherUid: Long): String {
    val suffix = publishedAt.trim()
        .takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
        ?.let { "发布于 $it" }
        ?: memberLabel.trim().takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    return listOfNotNull("UID $publisherUid", suffix).joinToString(" · ")
}

data class ProjectDetail(
    val summary: ProjectSummary,
    val images: List<ProjectImage>,
    val contacts: List<ProjectContact>,
    val publisherUid: Long,
    val status: String = "published",
    val rejectionReason: String = "",
    val rowVersion: Int = 0,
    val allowedExternalHosts: Set<String> = setOf("stk.zz-yihao.com"),
    val ownerActions: Set<String> = emptySet(),
    val fromCache: Boolean = false,
)

data class ProjectPage(
    val items: List<ProjectSummary>,
    val nextCursor: String?,
    val fromCache: Boolean = false,
    val emptyText: String = "暂无项目",
)

class ProjectException(
    val code: Int,
    override val message: String,
    val requestId: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
