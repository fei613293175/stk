package com.zzyihao.stk.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zzyihao.stk.data.project.ProjectCategory
import com.zzyihao.stk.data.project.ProjectDetail
import com.zzyihao.stk.data.project.ProjectException
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.data.project.ProjectSummary
import com.zzyihao.stk.data.project.ProjectPaging
import com.zzyihao.stk.data.auth.AuthException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProjectFailureKind { Info, Offline, Network, Timeout, NotFound, Forbidden, Service, Unknown }

data class ProjectFailure(
    val kind: ProjectFailureKind,
    val message: String,
    val code: Int? = null,
    val requestId: String? = null,
)

data class ProjectListState(
    val items: List<ProjectSummary> = emptyList(),
    val categories: List<ProjectCategory> = emptyList(),
    val query: String = "",
    val categoryId: String? = null,
    val nextCursor: String? = null,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
    val failure: ProjectFailure? = null,
    val pagingFailure: ProjectFailure? = null,
    val categoryFailure: ProjectFailure? = null,
    val offline: Boolean = false,
    val emptyText: String = "暂无项目",
)

data class ProjectDetailState(
    val detail: ProjectDetail? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val failure: ProjectFailure? = null,
)

class ProjectViewModel(private val repository: ProjectRepository, loadInitialList: Boolean = true) : ViewModel() {
    private val _list = MutableStateFlow(ProjectListState())
    val list: StateFlow<ProjectListState> = _list.asStateFlow()
    private val _detail = MutableStateFlow(ProjectDetailState())
    val detail: StateFlow<ProjectDetailState> = _detail.asStateFlow()
    private var loadJob: Job? = null

    init {
        if (loadInitialList) {
            loadCategories()
            loadFirstPage()
        }
    }

    fun updateQuery(value: String) { _list.update { it.copy(query = value) } }
    fun loadCategories() {
        viewModelScope.launch {
            runCatching { repository.listCategories() }
                .onSuccess { categories -> _list.update { it.copy(categories = categories, categoryFailure = null) } }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    _list.update { it.copy(categoryFailure = error.toProjectFailure()) }
                }
        }
    }
    fun loadFirstPage(refresh: Boolean = false) {
        loadJob?.cancel(); loadJob = viewModelScope.launch {
            _list.update { it.copy(loading = !refresh, refreshing = refresh, error = null, failure = null, pagingFailure = null) }
            runCatching { repository.listProjects(_list.value.query, _list.value.categoryId, null, refresh) }
                .onSuccess { page -> _list.update { it.copy(items = page.items, nextCursor = page.nextCursor, loading = false, refreshing = false, offline = page.fromCache, emptyText = page.emptyText, error = null, failure = null) } }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    val failure = error.toProjectFailure()
                    _list.update { it.copy(loading = false, refreshing = false, error = failure.message, failure = failure) }
                }
        }
    }
    fun loadMore() {
        val cursor = _list.value.nextCursor ?: return
        if (_list.value.loadingMore) return
        viewModelScope.launch {
            _list.update { it.copy(loadingMore = true, pagingFailure = null) }
            runCatching { repository.listProjects(_list.value.query, _list.value.categoryId, cursor) }
                .onSuccess { page -> _list.update { current -> current.copy(items = ProjectPaging.merge(current.items, page.items), nextCursor = page.nextCursor, loadingMore = false, offline = page.fromCache, pagingFailure = null) } }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    _list.update { it.copy(loadingMore = false, pagingFailure = error.toProjectFailure()) }
                }
        }
    }
    fun setCategory(categoryId: String?) { _list.update { it.copy(categoryId = categoryId) }; loadFirstPage(true) }
    fun search() = loadFirstPage(true)
    fun clearError() { _list.update { it.copy(error = null, failure = null, pagingFailure = null) } }
    fun openDetail(id: String) {
        viewModelScope.launch {
            _detail.value = ProjectDetailState(loading = true)
            runCatching {
                repository.getProjectDetail(id).also { detail ->
                    if (detail.status == "published") {
                        runCatching { repository.recordProjectView(id) }
                            .onFailure { it.rethrowIfCancellation() }
                    }
                }
            }
                .onSuccess { _detail.value = ProjectDetailState(detail = it) }
                .onFailure { error ->
                    error.rethrowIfCancellation()
                    val failure = error.toProjectFailure()
                    _detail.value = ProjectDetailState(error = failure.message, failure = failure)
                }
        }
    }
    fun clearDetail() { _detail.value = ProjectDetailState() }
    fun retryDetail(id: String) = openDetail(id)

    class Factory(private val repository: ProjectRepository, private val loadInitialList: Boolean = true) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T { require(modelClass.isAssignableFrom(ProjectViewModel::class.java)); return ProjectViewModel(repository, loadInitialList) as T }
    }
}

private fun Throwable.toProjectFailure(): ProjectFailure {
    val code = when (this) {
        is ProjectException -> this.code
        is AuthException -> this.code
        else -> null
    }
    val requestId = when (this) {
        is ProjectException -> this.requestId
        is AuthException -> this.requestId
        else -> null
    }
    val kind = when (code) {
        5003 -> ProjectFailureKind.Offline
        5002 -> ProjectFailureKind.Timeout
        5001 -> ProjectFailureKind.Network
        4040 -> ProjectFailureKind.NotFound
        4030 -> ProjectFailureKind.Forbidden
        5000 -> ProjectFailureKind.Service
        else -> if (code != null && code >= 5000) ProjectFailureKind.Service else ProjectFailureKind.Unknown
    }
    return ProjectFailure(kind, message ?: "项目加载失败", code, requestId)
}
