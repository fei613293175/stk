package com.zzyihao.stk.data.project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PreparedImage(val uri: Uri, val mimeType: String, val sizeBytes: Long, val displayName: String)

object ImageUploadProcessor {
    const val MaxImages = 9
    const val MaxImageBytes = 10L * 1024L * 1024L
    val AllowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    suspend fun prepare(
        context: Context,
        uris: List<Uri>,
        maxImages: Int = MaxImages,
        maxImageBytes: Long = MaxImageBytes,
        longEdge: Int = 1920,
        jpegQuality: Int = 82,
        allowedMimeTypes: Set<String> = AllowedMimeTypes,
    ): Result<List<PreparedImage>> = withContext(Dispatchers.IO) {
        runCatching {
            require(uris.isNotEmpty()) { "请至少选择 1 张图片" }
            require(uris.size <= maxImages) { "最多选择 $maxImages 张图片" }
            val resolver = context.contentResolver
            val outputDirectory = File(context.cacheDir, "project-images").apply { mkdirs() }
            uris.mapIndexed { index, uri ->
                val mime = resolver.getType(uri)?.lowercase().orEmpty()
                require(mime in allowedMimeTypes) { "第 ${index + 1} 张图片格式不支持" }
                val originalSize = (resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L).takeIf { it >= 0 } ?: 0L
                require(originalSize in 1..maxImageBytes) { "第 ${index + 1} 张图片超过后台限制或无法读取" }

                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                require(bounds.outWidth > 0 && bounds.outHeight > 0) { "第 ${index + 1} 张图片无法解码" }
                var sampleSize = 1
                while (bounds.outWidth / sampleSize > longEdge * 2 || bounds.outHeight / sampleSize > longEdge * 2) sampleSize *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                    ?: error("第 ${index + 1} 张图片无法读取")
                val scale = minOf(1f, longEdge.toFloat() / maxOf(decoded.width, decoded.height).toFloat())
                val prepared = if (scale < 1f) Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt().coerceAtLeast(1), (decoded.height * scale).toInt().coerceAtLeast(1), true) else decoded
                val file = File(outputDirectory, "${System.currentTimeMillis()}-$index-${uri.toString().hashCode().toUInt().toString(16)}.jpg")
                FileOutputStream(file).use { output -> require(prepared.compress(Bitmap.CompressFormat.JPEG, jpegQuality, output)) { "第 ${index + 1} 张图片压缩失败" } }
                if (prepared !== decoded) prepared.recycle()
                decoded.recycle()
                require(file.length() in 1..maxImageBytes) { "第 ${index + 1} 张图片压缩后仍超过后台限制" }
                PreparedImage(Uri.fromFile(file), "image/jpeg", file.length(), "图片 ${index + 1}")
            }
        }
    }
}
