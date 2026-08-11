package com.zzyihao.stk.data.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectDraftImagesTest {
    @Test
    fun movingAnImageNormalizesOrderAndCover() {
        val result = ProjectDraftImages.move(listOf(image("a", 0), image("b", 1), image("c", 2)), 2, -1)

        assertEquals(listOf("a", "c", "b"), result.map { it.localUri })
        assertEquals(listOf(0, 1, 2), result.map { it.sortOrder })
        assertTrue(result[0].isCover)
    }

    @Test
    fun settingCoverMovesItToTheFirstSubmissionPosition() {
        val result = ProjectDraftImages.setCover(listOf(image("a", 0), image("b", 1)), 1)

        assertEquals(listOf("b", "a"), result.map { it.localUri })
        assertTrue(result[0].isCover)
    }

    @Test
    fun normalizingAnEditorSequenceDoesNotRestoreStaleSortOrders() {
        val result = ProjectDraftImages.normalize(listOf(image("c", 2), image("a", 0), image("b", 1)))

        assertEquals(listOf("c", "a", "b"), result.map { it.localUri })
        assertEquals(listOf(0, 1, 2), result.map { it.sortOrder })
        assertTrue(result[0].isCover)
    }

    private fun image(uri: String, order: Int) = ProjectImageDraft(
        localUri = uri,
        mimeType = "image/jpeg",
        sizeBytes = 1,
        displayName = uri,
        sortOrder = order,
        isCover = order == 0,
    )
}
