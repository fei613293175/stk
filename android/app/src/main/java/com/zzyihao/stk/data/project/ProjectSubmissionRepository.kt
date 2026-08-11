package com.zzyihao.stk.data.project

interface ProjectSubmissionRepository {
    suspend fun getPublishingConfig(): PublishingConfig
    suspend fun uploadImage(image: ProjectImageDraft, onProgress: (Int) -> Unit = {}): ProjectImageDraft
    suspend fun deleteUploadedImage(image: ProjectImageDraft)
    suspend fun listMyProjects(status: MyProjectStatus = MyProjectStatus.ALL, cursor: String? = null): MyProjectPage
    suspend fun getMyProject(id: String): MyProject
    suspend fun createProject(draft: ProjectDraft, onProgress: (Int) -> Unit = {}): MyProject
    suspend fun updateProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit = {}): MyProject
    suspend fun unpublishProject(id: String): MyProject
    suspend fun resubmitProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit = {}): MyProject
    suspend fun softDeleteProject(id: String)
}
