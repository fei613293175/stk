package com.zzyihao.stk.data.project

import kotlinx.coroutines.delay
import java.util.UUID

class FakeProjectSubmissionRepository : ProjectSubmissionRepository {
    private val projects = mutableListOf(
        MyProject("mine-1001", "我的待审核项目", "已提交审核的测试项目。", "service", "渠道推广", MyProjectStatus.PENDING, updatedAt = "2026-08-07", ownerActions = setOf("view", "edit", "delete")),
        MyProject("mine-1002", "我的驳回项目", "需要补充图片说明的测试项目。", "retail", "本地生活", MyProjectStatus.REJECTED, "请补充清晰的项目封面和联系方式。", "2026-08-07", ownerActions = setOf("view", "edit", "resubmit", "delete")),
        MyProject("mine-1003", "我的已发布项目", "已通过审核的测试项目。", "technology", "企业服务", MyProjectStatus.PUBLISHED, updatedAt = "2026-08-06", ownerActions = setOf("view", "edit", "offline")),
    )

    override suspend fun getPublishingConfig(): PublishingConfig = PublishingConfig()

    override suspend fun uploadImage(image: ProjectImageDraft, onProgress: (Int) -> Unit): ProjectImageDraft {
        onProgress(10)
        delay(120)
        onProgress(100)
        return image.copy(storageKey = "data/attachment/stk_project/fake/${UUID.randomUUID()}.jpg", uploadProgress = 100, uploadError = null)
    }

    override suspend fun deleteUploadedImage(image: ProjectImageDraft) { delay(50) }

    override suspend fun listMyProjects(status: MyProjectStatus, cursor: String?): MyProjectPage {
        delay(180)
        val offset = cursor?.toIntOrNull() ?: 0
        val filtered = projects.filter { status == MyProjectStatus.ALL || it.status == status }
        val page = filtered.drop(offset).take(10)
        val next = (offset + page.size).takeIf { it < filtered.size }?.toString()
        return MyProjectPage(page, next)
    }

    override suspend fun getMyProject(id: String): MyProject = projects.firstOrNull { it.id == id } ?: throw ProjectSubmissionException(4040, "项目不存在")

    override suspend fun createProject(draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject {
        return save(draft, null, onProgress)
    }

    override suspend fun updateProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject {
        requireProject(id)
        return save(draft, id, onProgress)
    }

    override suspend fun resubmitProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject {
        requireProject(id)
        return save(draft, id, onProgress)
    }

    override suspend fun unpublishProject(id: String): MyProject {
        delay(160)
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) throw ProjectSubmissionException(4040, "项目不存在")
        return projects[index].copy(status = MyProjectStatus.UNPUBLISHED, updatedAt = "2026-08-07", ownerActions = setOf("view", "edit", "delete"))
            .also { projects[index] = it }
    }

    override suspend fun softDeleteProject(id: String) {
        delay(160)
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) throw ProjectSubmissionException(4040, "项目不存在")
        projects[index] = projects[index].copy(status = MyProjectStatus.DELETED, updatedAt = "2026-08-07")
    }

    private suspend fun save(draft: ProjectDraft, id: String?, onProgress: (Int) -> Unit): MyProject {
        validate(draft)
        for (progress in listOf(10, 35, 65, 100)) { delay(120); onProgress(progress) }
        val existing = id?.let { projectId -> projects.firstOrNull { it.id == projectId } }
        val item = MyProject(
            id ?: "mine-${UUID.randomUUID()}",
            draft.title.trim(),
            draft.summary.trim(),
            draft.categoryId,
            categoryName(draft.categoryId),
            MyProjectStatus.PENDING,
            updatedAt = "2026-08-07",
            images = draft.images,
            rowVersion = (existing?.rowVersion ?: 0) + 1,
            contactName = draft.contactName.trim(),
            phone = draft.phone.trim(),
            wechat = draft.wechat.trim(),
            qq = draft.qq.trim(),
            website = draft.website.trim(),
            ownerActions = setOf("view", "edit", "delete"),
        )
        val index = projects.indexOfFirst { it.id == id }
        if (index >= 0) projects[index] = item else projects.add(0, item)
        return item
    }

    private fun requireProject(id: String) { if (projects.none { it.id == id }) throw ProjectSubmissionException(4040, "项目不存在") }
    private fun validate(draft: ProjectDraft) {
        if (draft.title.trim().length !in 4..60) throw ProjectSubmissionException(4001, "标题长度必须为 4 至 60 个字符")
        if (draft.summary.trim().length !in 10..1000) throw ProjectSubmissionException(4001, "简介长度必须为 10 至 1000 个字符")
        if (draft.categoryId.isBlank()) throw ProjectSubmissionException(4001, "请选择项目分类")
        if (draft.images.isEmpty()) throw ProjectSubmissionException(4001, "请至少选择 1 张图片")
        if (draft.images.size > ImageUploadProcessor.MaxImages) throw ProjectSubmissionException(4001, "图片数量超过上限")
        if (draft.contactName.trim().isBlank() || listOf(draft.phone, draft.wechat, draft.qq, draft.website).none { it.isNotBlank() }) {
            throw ProjectSubmissionException(4001, "请填写联系人和联系方式")
        }
    }
    private fun categoryName(id: String) = mapOf("retail" to "本地生活", "service" to "渠道推广", "technology" to "企业服务")[id].orEmpty()
}
