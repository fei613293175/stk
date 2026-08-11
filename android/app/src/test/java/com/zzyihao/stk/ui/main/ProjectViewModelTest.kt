package com.zzyihao.stk.ui.main

import com.zzyihao.stk.data.project.ProjectCategory
import com.zzyihao.stk.data.project.ProjectContact
import com.zzyihao.stk.data.project.ProjectDetail
import com.zzyihao.stk.data.project.ProjectImage
import com.zzyihao.stk.data.project.ProjectPage
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.data.project.ProjectSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun publishedDetailRecordsView() = runTest(dispatcher) {
        val repository = DetailRepository("published")
        val viewModel = ProjectViewModel(repository)
        advanceUntilIdle()

        viewModel.openDetail("42")
        advanceUntilIdle()

        assertEquals(1, repository.viewCalls)
    }

    @Test
    fun nonPublishedDetailDoesNotRecordView() = runTest(dispatcher) {
        val repository = DetailRepository("pending")
        val viewModel = ProjectViewModel(repository)
        advanceUntilIdle()

        viewModel.openDetail("42")
        advanceUntilIdle()

        assertEquals(0, repository.viewCalls)
    }

    @Test
    fun cancelledFirstPageRequestDoesNotBecomeVisibleError() = runTest(dispatcher) {
        val repository = CancellingProjectRepository()
        val viewModel = ProjectViewModel(repository)
        runCurrent()

        viewModel.setCategory("service")
        runCurrent()
        repository.releaseCancelledCall.complete(Unit)
        runCurrent()

        assertNull(viewModel.list.value.error)
        assertEquals("service", viewModel.list.value.categoryId)

        repository.secondPage.complete(ProjectPage(emptyList(), null))
        advanceUntilIdle()
        assertNull(viewModel.list.value.error)
    }
}

private class CancellingProjectRepository : ProjectRepository {
    val releaseCancelledCall = CompletableDeferred<Unit>()
    val secondPage = CompletableDeferred<ProjectPage>()
    private var listCalls = 0

    override suspend fun listCategories() = listOf(ProjectCategory("all", "全部"), ProjectCategory("service", "服务"))

    override suspend fun listProjects(query: String, categoryId: String?, cursor: String?, refresh: Boolean): ProjectPage {
        listCalls += 1
        if (listCalls == 1) {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) { releaseCancelledCall.await() }
            }
        }
        return secondPage.await()
    }

    override suspend fun getProjectDetail(projectId: String) = error("Not used")
    override suspend fun recordProjectView(projectId: String) = Unit
}

private class DetailRepository(private val status: String) : ProjectRepository {
    var viewCalls = 0

    override suspend fun listCategories() = listOf(ProjectCategory("all", "全部"))
    override suspend fun listProjects(query: String, categoryId: String?, cursor: String?, refresh: Boolean) =
        ProjectPage(emptyList(), null)

    override suspend fun getProjectDetail(projectId: String) = ProjectDetail(
        summary = ProjectSummary(projectId, "项目标题", "项目简介", "service", "服务", "", "发布者", "普通用户", "", 0),
        images = emptyList<ProjectImage>(),
        contacts = emptyList<ProjectContact>(),
        publisherUid = 1,
        status = status,
    )

    override suspend fun recordProjectView(projectId: String) {
        viewCalls += 1
    }
}
