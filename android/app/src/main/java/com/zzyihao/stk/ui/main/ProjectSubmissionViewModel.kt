package com.zzyihao.stk.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.MyProjectStatus
import com.zzyihao.stk.data.project.ProjectDraft
import com.zzyihao.stk.data.project.ProjectImageDraft
import com.zzyihao.stk.data.project.ProjectDraftImages
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MyProjectsState(
    val items: List<MyProject> = emptyList(),
    val status: MyProjectStatus = MyProjectStatus.ALL,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val statusCounts: Map<MyProjectStatus, Int> = emptyMap(),
    val fromCache: Boolean = false,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
)

data class SubmissionState(
    val draft: ProjectDraft = ProjectDraft(),
    val submitting: Boolean = false,
    val progress: Int = 0,
    val message: String? = null,
    val error: String? = null,
    val errorCode: Int? = null,
    val dirty: Boolean = false,
    val removingImageUri: String? = null,
    val loadingDraft: Boolean = false,
    val editLoadError: String? = null,
    val editLoadErrorCode: Int? = null,
)

class ProjectSubmissionViewModel(private val repository: ProjectSubmissionRepository) : ViewModel() {
    private val _mine = MutableStateFlow(MyProjectsState())
    val mine: StateFlow<MyProjectsState> = _mine.asStateFlow()
    private val _submission = MutableStateFlow(SubmissionState())
    val submission: StateFlow<SubmissionState> = _submission.asStateFlow()
    private var listJob: Job? = null

    init { loadMine() }
    fun setDraft(draft: ProjectDraft) {
        _submission.update { current ->
            val changed = current.draft.copy(requestId = "") != draft.copy(requestId = "")
            current.copy(
                draft = if (changed) draft.copy(requestId = UUID.randomUUID().toString()) else draft,
                error = null,
                errorCode = null,
                message = null,
                dirty = true,
            )
        }
    }
    fun addImages(images: List<ProjectImageDraft>) {
        if (images.isEmpty()) return
        _submission.update { state ->
            state.copy(
                draft = state.draft.copy(images = ProjectDraftImages.normalize(state.draft.images + images), requestId = UUID.randomUUID().toString()),
                error = null,
                dirty = true,
            )
        }
        images.forEach(::uploadImage)
    }
    fun retryImage(localUri: String) {
        _submission.value.draft.images.firstOrNull { it.localUri == localUri }?.let(::uploadImage)
    }
    fun removeImage(index: Int) {
        val image = _submission.value.draft.images.getOrNull(index) ?: return
        viewModelScope.launch {
            _submission.update { it.copy(removingImageUri = image.localUri, error = null) }
            runCatching { repository.deleteUploadedImage(image) }
                .onSuccess {
                    _submission.update { state ->
                        state.copy(
                            draft = state.draft.copy(images = ProjectDraftImages.remove(state.draft.images, index), requestId = UUID.randomUUID().toString()),
                            dirty = true,
                            removingImageUri = null,
                        )
                    }
                }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    _submission.update { it.copy(removingImageUri = null, error = error.message ?: "删除图片失败，请重试") }
                }
        }
    }
    private fun uploadImage(image: ProjectImageDraft) {
        viewModelScope.launch {
            updateImage(image.localUri) { it.copy(uploadProgress = 1, uploadError = null) }
            runCatching {
                repository.uploadImage(image) { progress ->
                    updateImage(image.localUri) { it.copy(uploadProgress = progress, uploadError = null) }
                }
            }.onSuccess { uploaded ->
                updateImage(image.localUri, invalidatesSubmission = true) { uploaded.copy(sortOrder = it.sortOrder, isCover = it.isCover) }
            }.onFailure { error ->
                error.rethrowIfCancellation()
                updateImage(image.localUri) { it.copy(uploadProgress = 0, uploadError = error.message ?: "图片上传失败") }
            }
        }
    }
    private fun updateImage(localUri: String, invalidatesSubmission: Boolean = false, transform: (ProjectImageDraft) -> ProjectImageDraft) {
        _submission.update { state ->
            val draft = state.draft.copy(images = state.draft.images.map { if (it.localUri == localUri) transform(it) else it })
            state.copy(draft = if (invalidatesSubmission) draft.copy(requestId = UUID.randomUUID().toString()) else draft)
        }
    }
    fun resetDraft() { _submission.value = SubmissionState() }
    fun loadDraft(project: MyProject) {
        viewModelScope.launch {
            _submission.update { it.copy(loadingDraft = true, editLoadError = null, editLoadErrorCode = null, error = null, errorCode = null, dirty = false) }
            runCatching { repository.getMyProject(project.id) }
                .onSuccess { loaded ->
                    _submission.value = SubmissionState(
                        draft = ProjectDraft(
                    id = loaded.id,
                    title = loaded.title,
                    summary = loaded.summary,
                    categoryId = loaded.categoryId,
                    images = loaded.images,
                    resubmit = loaded.status == MyProjectStatus.REJECTED,
                    rowVersion = loaded.rowVersion,
                    contactName = loaded.contactName,
                    phone = loaded.phone,
                    wechat = loaded.wechat,
                    qq = loaded.qq,
                    website = loaded.website,
                        ),
                        dirty = false,
                    )
                }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    val submissionError = error as? com.zzyihao.stk.data.project.ProjectSubmissionException
                    _submission.update { it.copy(
                        loadingDraft = false,
                        editLoadError = error.message ?: "项目加载失败",
                        editLoadErrorCode = submissionError?.code,
                    ) }
                }
        }
    }
    fun loadMine(status: MyProjectStatus = _mine.value.status) {
        listJob?.cancel(); listJob = viewModelScope.launch {
            _mine.update { it.copy(status = status, loading = true, error = null, nextCursor = null) }
            runCatching { repository.listMyProjects(status) }
                .onSuccess { page -> _mine.update { it.copy(items = page.items, nextCursor = page.nextCursor, hasMore = page.hasMore, statusCounts = page.statusCounts, fromCache = page.fromCache, loading = false) } }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    _mine.update { it.copy(loading = false, error = error.message ?: "我的发布加载失败") }
                }
        }
    }
    fun loadMoreMine() {
        val cursor = _mine.value.nextCursor ?: return
        if (_mine.value.loadingMore) return
        viewModelScope.launch {
            _mine.update { it.copy(loadingMore = true, error = null) }
            runCatching { repository.listMyProjects(_mine.value.status, cursor) }
                .onSuccess { page -> _mine.update { state -> state.copy(items = (state.items + page.items).distinctBy { it.id }, nextCursor = page.nextCursor, hasMore = page.hasMore, statusCounts = page.statusCounts.ifEmpty { state.statusCounts }, fromCache = page.fromCache, loadingMore = false) } }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    _mine.update { it.copy(loadingMore = false, error = error.message ?: "加载更多失败") }
                }
        }
    }
    fun submit(onSuccess: (MyProject) -> Unit) {
        val current = _submission.value
        if (current.submitting) return
        viewModelScope.launch {
                _submission.update { it.copy(submitting = true, progress = 0, error = null, errorCode = null, message = null) }
            runCatching {
                val callback: (Int) -> Unit = { progress -> _submission.update { it.copy(progress = progress) } }
                if (current.draft.id == null) repository.createProject(current.draft, callback)
                else if (current.draft.resubmit) repository.resubmitProject(current.draft.id, current.draft, callback)
                else repository.updateProject(current.draft.id, current.draft, callback)
            }.onSuccess { project ->
                _submission.update { it.copy(submitting = false, progress = 100, message = if (project.status == MyProjectStatus.PUBLISHED) "已保存并继续发布" else "已提交审核") }
                loadMine(); onSuccess(project)
            }.onFailure { error ->
                error.rethrowIfCancellation()
                _submission.update { it.copy(submitting = false, error = error.message ?: "提交失败", errorCode = (error as? com.zzyihao.stk.data.project.ProjectSubmissionException)?.code) }
            }
        }
    }
    fun unpublish(project: MyProject) { action { repository.unpublishProject(project.id) } }
    fun softDelete(project: MyProject) { action { repository.softDeleteProject(project.id) } }
    private fun action(block: suspend () -> Any) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { loadMine() }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    _mine.update { it.copy(error = error.message ?: "操作失败") }
                }
        }
    }

    class Factory(private val repository: ProjectSubmissionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T { require(modelClass.isAssignableFrom(ProjectSubmissionViewModel::class.java)); return ProjectSubmissionViewModel(repository) as T }
    }
}
