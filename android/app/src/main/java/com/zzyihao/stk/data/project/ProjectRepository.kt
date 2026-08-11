package com.zzyihao.stk.data.project

interface ProjectRepository {
    suspend fun listCategories(): List<ProjectCategory>
    suspend fun listProjects(
        query: String = "",
        categoryId: String? = null,
        cursor: String? = null,
        refresh: Boolean = false,
    ): ProjectPage
    suspend fun getProjectDetail(projectId: String): ProjectDetail
    suspend fun recordProjectView(projectId: String)
}
