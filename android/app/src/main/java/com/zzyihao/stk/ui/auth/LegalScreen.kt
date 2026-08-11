package com.zzyihao.stk.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.zzyihao.stk.data.legal.LegalDocumentContent
import com.zzyihao.stk.data.legal.LegalRepository
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

enum class LegalDocument(val documentId: String, val fallbackTitle: String) {
    UserAgreement("user_agreement", "用户协议"),
    PrivacyPolicy("privacy_policy", "隐私政策"),
}

private sealed interface LegalLoadState {
    data object Loading : LegalLoadState
    data class Ready(val document: LegalDocumentContent) : LegalLoadState
    data class Error(val message: String) : LegalLoadState
}

@Composable
fun LegalScreen(document: LegalDocument, repository: LegalRepository, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var retryVersion by rememberSaveable { mutableIntStateOf(0) }
    val state by produceState<LegalLoadState>(
        initialValue = LegalLoadState.Loading,
        document.documentId,
        repository,
        retryVersion,
    ) {
        value = runCatching { repository.getDocument(document.documentId) }
            .fold(
                onSuccess = { LegalLoadState.Ready(it) },
                onFailure = { LegalLoadState.Error(it.message ?: "无法读取版本化文档") },
            )
    }
    Column(Modifier.fillMaxSize().background(StkColors.Background).padding(top = StkDimens.SpaceSm)) {
        StkTopBar(
            title = document.fallbackTitle,
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        when (val current = state) {
            LegalLoadState.Loading -> LegalSkeleton()
            is LegalLoadState.Ready -> LegalContent(document = current.document)
            is LegalLoadState.Error -> LegalError(message = current.message, onRetry = { retryVersion++ })
        }
    }
}

@Composable
private fun LegalSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(StkDimens.SpaceBase),
        verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceXl),
    ) {
        SkeletonLine(0.46f)
        SkeletonLine(0.96f)
        SkeletonLine(1f)
        SkeletonLine(0.9f)
        SkeletonLine(0.98f)
        SkeletonLine(0.84f)
        SkeletonLine(1f)
        SkeletonLine(0.94f)
        SkeletonLine(0.88f)
        SkeletonLine(0.8f)
    }
}

@Composable
private fun SkeletonLine(fraction: Float) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(StkDimens.SpaceBase)
            .clip(RoundedCornerShape(StkDimens.RadiusSmall))
            .background(StkColors.Border.copy(alpha = 0.42f)),
    )
}

@Composable
private fun LegalContent(document: LegalDocumentContent) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StkDimens.SpaceBase),
        verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
    ) {
        if (document.cached) {
            StkFeedbackBanner(
                message = "当前显示已缓存版本，网络恢复后将自动检查更新。",
                tone = StkFeedbackTone.Warning,
                modifier = Modifier.padding(bottom = StkDimens.SpaceMd),
            )
        }
        Text(document.title, style = MaterialTheme.typography.headlineLarge, color = StkColors.TextPrimary)
        Text(
            buildString {
                append("版本：")
                append(document.version)
                append(" · 生效日期：")
                append(document.effectiveDate ?: "以后台发布为准")
            },
            style = MaterialTheme.typography.bodySmall,
            color = StkColors.TextTertiary,
            modifier = Modifier.padding(bottom = StkDimens.SpaceLg),
        )
        document.content
            .split(Regex("\\n\\s*\\n|\\n"))
            .map { it.trim() }
            .filter(String::isNotBlank)
            .forEach { paragraph ->
                val heading = paragraph.trimStart('#', ' ').matches(Regex("^[一二三四五六七八九十]+、.*")) ||
                    paragraph.startsWith("#")
                Text(
                    text = paragraph.trimStart('#', ' '),
                    style = if (heading) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    color = if (heading) StkColors.TextPrimary else StkColors.TextSecondary,
                    modifier = if (heading) Modifier.padding(top = StkDimens.SpaceMd) else Modifier,
                )
            }
    }
}

@Composable
private fun LegalError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(StkDimens.SpaceBase),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(StkDimens.SystemFeedbackIcon).background(StkColors.ErrorSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = StkColors.Error,
                modifier = Modifier.size(StkDimens.SystemFeedbackInnerIcon),
            )
        }
        Text(
            "内容加载失败",
            style = MaterialTheme.typography.headlineLarge,
            color = StkColors.TextPrimary,
            modifier = Modifier.padding(top = StkDimens.SpaceXl),
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = StkColors.TextSecondary,
            modifier = Modifier.padding(top = StkDimens.SpaceSm),
        )
        StkPrimaryButton(
            text = "重新加载",
            onClick = onRetry,
            modifier = Modifier.width(StkDimens.EmptyStateButtonWidth).padding(top = StkDimens.SpaceXl),
        )
    }
}
