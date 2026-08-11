package com.zzyihao.stk.data.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectPagingTest {
    @Test fun mergeRemovesDuplicateProjectIds() {
        val first = item("1")
        assertEquals(listOf("1", "2"), ProjectPaging.merge(listOf(first), listOf(first, item("2"))).map { it.id })
    }
    @Test fun externalUrlRequiresAllowedHost() {
        assertTrue(ProjectPaging.isAllowedExternalUrl("https://stk.zz-yihao.com/project/1"))
        assertFalse(ProjectPaging.isAllowedExternalUrl("https://example.com/project/1"))
        assertFalse(ProjectPaging.isAllowedExternalUrl("http://stk.zz-yihao.com/project/1"))
        assertFalse(ProjectPaging.isAllowedExternalUrl("https://stk.zz-yihao.com.example.com/project/1"))
        assertFalse(ProjectPaging.isAllowedExternalUrl("not-a-url"))
        assertTrue(ProjectPaging.isAllowedExternalUrl("https://partner.example.com/project/1", setOf("partner.example.com")))
        assertFalse(ProjectPaging.isAllowedExternalUrl("https://partner.example.com/project/1", setOf("stk.zz-yihao.com")))
    }
    @Test fun contactTargetsAreTypeChecked() {
        assertTrue(ProjectPaging.isValidContactTarget(ProjectContact("phone", "+86 138-0000-8000", "手机")))
        assertTrue(ProjectPaging.isValidContactTarget(ProjectContact("wechat", "stk_fixture_001", "微信")))
        assertTrue(ProjectPaging.isValidContactTarget(ProjectContact("qq", "100001", "QQ")))
        assertFalse(ProjectPaging.isValidContactTarget(ProjectContact("qq", "javascript:alert(1)", "QQ")))
        assertFalse(ProjectPaging.isValidContactTarget(ProjectContact("unknown", "payload", "未知")))
    }
    private fun item(id: String) = ProjectSummary(id, "标题$id", "简介", "all", "全部", "", "发布者", "普通用户", "2026-08-06", 0)
}
