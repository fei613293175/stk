package com.zzyihao.stk.data.project

import kotlinx.coroutines.delay

class FakeProjectRepository(private val cache: ProjectCache) : ProjectRepository {
    private val categories = listOf(
        ProjectCategory("all", "全部", 0),
        // Fixture labels mirror the approved reference images; ids remain the backend contract.
        ProjectCategory("retail", "本地生活", 1),
        ProjectCategory("service", "渠道推广", 2),
        ProjectCategory("technology", "企业服务", 3),
    )

    private val projects = listOf(
        project("p-1001", "社区生鲜配送项目", "覆盖周边社区的生鲜到家服务，寻找区域合作伙伴。", "retail", "本地生活", "张先生", "普通用户", 126),
        project("p-1002", "企业短视频代运营", "提供脚本、拍摄和账号运营的一站式内容服务。", "service", "渠道推广", "李女士", "认证用户", 89),
        project("p-1003", "智能门店管理系统", "面向连锁门店的库存、会员与经营分析工具。", "technology", "企业服务", "王先生", "普通用户", 214),
        project("p-1004", "校园文创集合店", "原创文创产品与校园渠道合作，支持区域代理。", "retail", "本地生活", "陈女士", "普通用户", 72),
        project("p-1005", "家庭维修服务平台", "连接本地维修师傅与家庭用户的服务平台。", "service", "渠道推广", "赵先生", "认证用户", 165),
        project("p-1006", "AI 客服知识库", "帮助中小企业快速搭建可维护的客服知识库。", "technology", "企业服务", "周先生", "普通用户", 301),
        project("p-1007", "社区团购供应链", "为社区团长提供稳定货源与履约支持。", "retail", "本地生活", "刘女士", "普通用户", 118),
        project("p-1008", "企业培训课程合作", "面向企业客户的管理与销售培训课程。", "service", "渠道推广", "黄老师", "认证用户", 97),
    )

    override suspend fun listCategories(): List<ProjectCategory> {
        delay(120)
        cache.saveCategories(categories)
        return categories
    }

    override suspend fun listProjects(query: String, categoryId: String?, cursor: String?, refresh: Boolean): ProjectPage {
        delay(220)
        val filtered = projects.filter { item ->
            (categoryId.isNullOrBlank() || categoryId == "all" || item.categoryId == categoryId) &&
                (query.isBlank() || item.title.contains(query, true) || item.summary.contains(query, true))
        }
        val offset = cursor?.toIntOrNull() ?: 0
        val pageItems = filtered.drop(offset).take(4)
        val next = if (offset + pageItems.size < filtered.size) (offset + pageItems.size).toString() else null
        val page = ProjectPage(pageItems, next)
        if (offset == 0) cache.savePage(ProjectCache.pageKey(query, categoryId), page)
        pageItems.forEach { cache.saveDetail(detailFor(it)) }
        return page
    }

    override suspend fun getProjectDetail(projectId: String): ProjectDetail {
        delay(180)
        return projects.firstOrNull { it.id == projectId }?.let { detailFor(it) }
            ?: cache.readDetail(projectId)
            ?: throw ProjectException(4040, "项目不存在或已下架")
    }

    override suspend fun recordProjectView(projectId: String) { delay(30) }

    private fun detailFor(summary: ProjectSummary) = ProjectDetail(
        summary = summary,
        images = listOf(
            ProjectImage("${summary.id}-1", "https://images.unsplash.com/photo-1556761175-b413da4baf72?w=1200", summary.title),
            ProjectImage("${summary.id}-2", "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=1200", summary.title),
        ),
        contacts = listOf(
            ProjectContact("phone", "13800138000", "电话"),
            ProjectContact("wechat", "stk_demo", "微信"),
            ProjectContact("url", "https://stk.zz-yihao.com/project/${summary.id}", "项目网址"),
        ),
        publisherUid = 10000L + summary.id.takeLast(3).toLong(),
    )

    private fun project(id: String, title: String, summary: String, categoryId: String, categoryName: String, publisher: String, member: String, views: Int) = ProjectSummary(
        id, title, summary, categoryId, categoryName, "https://images.unsplash.com/photo-1556761175-b413da4baf72?w=800", publisher, member, "2026-08-06", views,
    )
}
