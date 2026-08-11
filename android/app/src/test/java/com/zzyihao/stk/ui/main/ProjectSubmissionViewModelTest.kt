package com.zzyihao.stk.ui.main

import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.MyProjectPage
import com.zzyihao.stk.data.project.MyProjectStatus
import com.zzyihao.stk.data.project.ProjectDraft
import com.zzyihao.stk.data.project.ProjectImageDraft
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import com.zzyihao.stk.data.project.PublishingConfig
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectSubmissionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun imageUploadSuccessStoresServerKey() = runTest(dispatcher) {
        val repository = RecordingRepository()
        val viewModel = ProjectSubmissionViewModel(repository)

        viewModel.addImages(listOf(image()))
        advanceUntilIdle()

        val uploaded = viewModel.submission.value.draft.images.single()
        assertEquals("data/attachment/stk/asset.jpg", uploaded.storageKey)
        assertEquals(100, uploaded.uploadProgress)
        assertNull(uploaded.uploadError)
    }

    @Test
    fun failedImageCanBeRetriedWithoutLosingDraft() = runTest(dispatcher) {
        val repository = RecordingRepository(uploadFailure = IllegalStateException("上传失败"))
        val viewModel = ProjectSubmissionViewModel(repository)

        viewModel.addImages(listOf(image()))
        advanceUntilIdle()
        assertEquals("上传失败", viewModel.submission.value.draft.images.single().uploadError)

        repository.uploadFailure = null
        viewModel.retryImage("content://image/1")
        advanceUntilIdle()

        val uploaded = viewModel.submission.value.draft.images.single()
        assertEquals("data/attachment/stk/asset.jpg", uploaded.storageKey)
        assertNull(uploaded.uploadError)
        assertEquals(2, repository.uploadAttempts)
    }

    @Test
    fun removingImageDeletesTemporaryAsset() = runTest(dispatcher) {
        val repository = RecordingRepository()
        val viewModel = ProjectSubmissionViewModel(repository)
        viewModel.addImages(listOf(image()))
        advanceUntilIdle()

        viewModel.removeImage(0)
        advanceUntilIdle()

        assertEquals(1, repository.deleteCalls)
        assertEquals(emptyList<ProjectImageDraft>(), viewModel.submission.value.draft.images)
    }

    @Test
    fun failedImageRemovalPreservesTheDraftImage() = runTest(dispatcher) {
        val repository = RecordingRepository(deleteFailure = IllegalStateException("删除失败"))
        val viewModel = ProjectSubmissionViewModel(repository)
        viewModel.addImages(listOf(image()))
        advanceUntilIdle()

        viewModel.removeImage(0)
        advanceUntilIdle()

        assertEquals(1, viewModel.submission.value.draft.images.size)
        assertEquals("删除失败", viewModel.submission.value.error)
        assertNull(viewModel.submission.value.removingImageUri)
    }

    @Test
    fun editingLoadFailureIsExposedWithoutReplacingTheDraft() = runTest(dispatcher) {
        val repository = RecordingRepository(projectLoadFailure = com.zzyihao.stk.data.project.ProjectSubmissionException(4040, "项目不存在"))
        val viewModel = ProjectSubmissionViewModel(repository)

        viewModel.loadDraft(MyProject("42", "项目标题", "项目简介内容足够长", "service", "服务", MyProjectStatus.PUBLISHED))
        advanceUntilIdle()

        assertEquals("项目不存在", viewModel.submission.value.editLoadError)
        assertEquals(4040, viewModel.submission.value.editLoadErrorCode)
        assertNull(viewModel.submission.value.draft.id)
    }

    @Test
    fun submitReturnsPendingServerState() = runTest(dispatcher) {
        val repository = RecordingRepository(createStatus = MyProjectStatus.PENDING)
        val viewModel = ProjectSubmissionViewModel(repository)
        var result: MyProject? = null
        viewModel.setDraft(ProjectDraft(title = "项目标题", summary = "项目简介内容足够长", categoryId = "service"))

        viewModel.submit { result = it }
        advanceUntilIdle()

        assertEquals(MyProjectStatus.PENDING, result?.status)
        assertEquals(100, viewModel.submission.value.progress)
        assertNotNull(viewModel.submission.value.message)
    }

    @Test
    fun submitReturnsPublishedServerState() = runTest(dispatcher) {
        val repository = RecordingRepository(createStatus = MyProjectStatus.PUBLISHED)
        val viewModel = ProjectSubmissionViewModel(repository)
        var result: MyProject? = null
        viewModel.setDraft(ProjectDraft(title = "项目标题", summary = "项目简介内容足够长", categoryId = "service"))

        viewModel.submit { result = it }
        advanceUntilIdle()

        assertEquals(MyProjectStatus.PUBLISHED, result?.status)
    }

    @Test
    fun cancelledListRequestDoesNotBecomeVisibleError() = runTest(dispatcher) {
        val repository = CancellingListRepository()
        val viewModel = ProjectSubmissionViewModel(repository)
        runCurrent()

        viewModel.loadMine(MyProjectStatus.PENDING)
        runCurrent()
        repository.releaseCancelledCall.complete(Unit)
        runCurrent()

        assertNull(viewModel.mine.value.error)
        assertEquals(MyProjectStatus.PENDING, viewModel.mine.value.status)

        repository.secondPage.complete(MyProjectPage(emptyList()))
        advanceUntilIdle()
        assertNull(viewModel.mine.value.error)
    }

    private fun image() = ProjectImageDraft(
        localUri = "content://image/1",
        mimeType = "image/jpeg",
        sizeBytes = 1024,
        displayName = "asset.jpg",
        sortOrder = 0,
        isCover = true,
    )
}

private class CancellingListRepository : RecordingRepository() {
    val releaseCancelledCall = CompletableDeferred<Unit>()
    val secondPage = CompletableDeferred<MyProjectPage>()
    private var listCalls = 0

    override suspend fun listMyProjects(status: MyProjectStatus, cursor: String?): MyProjectPage {
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
}

private open class RecordingRepository(
    var uploadFailure: Throwable? = null,
    var deleteFailure: Throwable? = null,
    var projectLoadFailure: Throwable? = null,
    private val createStatus: MyProjectStatus = MyProjectStatus.PENDING,
) : ProjectSubmissionRepository {
    var uploadAttempts = 0
    var deleteCalls = 0

    override suspend fun getPublishingConfig() = PublishingConfig()
    override suspend fun uploadImage(image: ProjectImageDraft, onProgress: (Int) -> Unit): ProjectImageDraft {
        uploadAttempts += 1
        uploadFailure?.let { throw it }
        onProgress(50)
        onProgress(100)
        return image.copy(storageKey = "data/attachment/stk/asset.jpg", uploadProgress = 100, uploadError = null)
    }
    override suspend fun deleteUploadedImage(image: ProjectImageDraft) { deleteCalls += 1; deleteFailure?.let { throw it } }
    open override suspend fun listMyProjects(status: MyProjectStatus, cursor: String?) = MyProjectPage(emptyList())
    override suspend fun getMyProject(id: String) = projectLoadFailure?.let { throw it } ?: project(MyProjectStatus.PENDING)
    override suspend fun createProject(draft: ProjectDraft, onProgress: (Int) -> Unit): MyProject { onProgress(100); return project(createStatus) }
    override suspend fun updateProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit) = project(MyProjectStatus.PENDING)
    override suspend fun unpublishProject(id: String) = project(MyProjectStatus.UNPUBLISHED)
    override suspend fun resubmitProject(id: String, draft: ProjectDraft, onProgress: (Int) -> Unit) = project(MyProjectStatus.PENDING)
    override suspend fun softDeleteProject(id: String) = Unit

    private fun project(status: MyProjectStatus) = MyProject("42", "项目标题", "项目简介内容足够长", "service", "服务", status)
}
