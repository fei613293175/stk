package com.zzyihao.stk.data.project

/** Keeps the client image contract canonical before it is submitted to the API. */
object ProjectDraftImages {
    /**
     * Reindex the order the editor is currently displaying.  Sorting here would undo a move or
     * cover selection because those operations change the List order before calling normalize.
     * Server data is sorted once when it is mapped into a draft.
     */
    fun normalize(images: List<ProjectImageDraft>): List<ProjectImageDraft> = images
        .mapIndexed { index, image -> image.copy(sortOrder = index, isCover = index == 0) }

    fun move(images: List<ProjectImageDraft>, index: Int, offset: Int): List<ProjectImageDraft> {
        val target = index + offset
        if (index !in images.indices || target !in images.indices) return normalize(images)
        return normalize(images.toMutableList().apply {
            val item = removeAt(index)
            add(target, item)
        })
    }

    fun setCover(images: List<ProjectImageDraft>, index: Int): List<ProjectImageDraft> {
        if (index !in images.indices) return normalize(images)
        return normalize(images.toMutableList().apply {
            add(0, removeAt(index))
        })
    }

    fun remove(images: List<ProjectImageDraft>, index: Int): List<ProjectImageDraft> =
        if (index !in images.indices) normalize(images) else normalize(images.filterIndexed { itemIndex, _ -> itemIndex != index })
}
