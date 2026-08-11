package com.zzyihao.stk.data.project

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectPresentationTest {
    @Test
    fun pendingProjectUsesUpdatedDateWhenPublishedDateIsMissing() {
        assertEquals("2026-08-11", normalizeProjectTimestamp(null, "2026-08-11"))
        assertEquals("2026-08-11", normalizeProjectTimestamp("null", "2026-08-11"))
    }

    @Test
    fun publisherMetaNeverRendersJsonNull() {
        val summary = projectSummary(publishedAt = "null", memberLabel = "普通用户")

        assertEquals("UID 9 · 普通用户", summary.publisherMeta(9))
    }

    @Test
    fun publisherMetaLabelsAvailableDate() {
        val summary = projectSummary(publishedAt = "2026-08-11", memberLabel = "普通用户")

        assertEquals("UID 9 · 发布于 2026-08-11", summary.publisherMeta(9))
    }

    private fun projectSummary(publishedAt: String, memberLabel: String) = ProjectSummary(
        id = "15",
        title = "STK10203FINAL",
        summary = "FINALDEVICEACCEPTANCE10203",
        categoryId = "3",
        categoryName = "科技",
        coverUrl = "https://stk.zz-yihao.com/example.jpg",
        publisherName = "stk_a68d44a66865",
        memberLabel = memberLabel,
        publishedAt = publishedAt,
        viewCount = 0,
    )
}
