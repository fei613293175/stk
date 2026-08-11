package com.zzyihao.stk.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.zzyihao.stk.data.project.ProjectSummary
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

@Composable
fun StkProjectCard(item: ProjectSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    StkInfoCard(modifier.clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                Box(
                    Modifier
                        .size(StkDimens.ProjectCardImageWidth, StkDimens.ProjectCardImageHeight)
                        .clip(RoundedCornerShape(StkDimens.RadiusControl)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.coverUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = item.coverUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (painter.state is AsyncImagePainter.State.Success) SubcomposeAsyncImageContent()
                            else ProjectCoverFallback(item.categoryName)
                        }
                    } else ProjectCoverFallback(item.categoryName)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = StkColors.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(item.summary, style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(item.publishedAt, style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary, maxLines = 1)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = StkDimens.SpaceSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(StkDimens.Icon).clip(CircleShape).background(StkColors.BrandPrimarySoft),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.publisherAvatarUrl.isNotBlank()) {
                        AsyncImage(item.publisherAvatarUrl, item.publisherName, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else Text(item.publisherName.takeLast(1), style = MaterialTheme.typography.labelMedium, color = StkColors.BrandPrimary)
                }
                Text(item.publisherName, Modifier.padding(start = StkDimens.SpaceSm), style = MaterialTheme.typography.labelMedium, color = StkColors.TextSecondary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.Visibility, null, tint = StkColors.TextTertiary, modifier = Modifier.size(StkDimens.IconSmall))
                Text(item.viewCount.toString(), Modifier.padding(start = StkDimens.SpaceXs), style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary)
            }
        }
    }
}

@Composable
private fun ProjectCoverFallback(categoryName: String) {
    Box(Modifier.fillMaxSize().background(StkColors.BrandPrimary), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(categoryName.ifBlank { "推广项目" }, style = MaterialTheme.typography.titleMedium, color = StkColors.Surface)
            Text("商推客项目", style = MaterialTheme.typography.labelMedium, color = StkColors.Surface)
        }
    }
}

@Composable
fun ProjectSkeletonList(count: Int = 3, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        repeat(count) { ProjectSkeletonCard() }
    }
}

@Composable
fun ProjectSkeletonCard() {
    StkInfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                SkeletonBlock(Modifier.size(StkDimens.ProjectCardImageWidth, StkDimens.ProjectCardImageHeight))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                    SkeletonBlock(Modifier.fillMaxWidth().height(StkDimens.SpaceBase))
                    SkeletonBlock(Modifier.fillMaxWidth().height(StkDimens.SpaceMd))
                    SkeletonBlock(Modifier.fillMaxWidth(0.68f).height(StkDimens.SpaceMd))
                }
            }
            SkeletonBlock(Modifier.width(StkDimens.AvatarCard).height(StkDimens.SpaceBase))
        }
    }
}

@Composable
fun SkeletonBlock(modifier: Modifier) {
    Box(modifier.background(StkColors.Border, RoundedCornerShape(StkDimens.RadiusSmall)))
}

@Composable
fun ProjectOfflineBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().background(StkColors.BrandAccentSoft, RoundedCornerShape(StkDimens.RadiusSmall)).padding(StkDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
    ) {
        Icon(Icons.Outlined.CloudOff, null, tint = StkColors.Warning, modifier = Modifier.size(StkDimens.IconSmall))
        Text(text, style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary)
    }
}

@Composable
fun ProjectFullPageState(
    title: String,
    message: String,
    buttonLabel: String?,
    onAction: () -> Unit,
    kind: ProjectFailureKind = ProjectFailureKind.Unknown,
    requestId: String? = null,
    modifier: Modifier = Modifier,
) {
    val warning = kind in setOf(ProjectFailureKind.Offline, ProjectFailureKind.Timeout, ProjectFailureKind.Forbidden)
    val informational = kind in setOf(ProjectFailureKind.Info, ProjectFailureKind.NotFound)
    val tint = if (warning) StkColors.Warning else if (informational) StkColors.BrandPrimary else StkColors.Error
    val soft = if (warning) StkColors.BrandAccentSoft else if (informational) StkColors.BrandPrimarySoft else StkColors.ErrorSoft
    val icon = when (kind) {
        ProjectFailureKind.Info -> Icons.Outlined.Info
        ProjectFailureKind.Offline -> Icons.Outlined.CloudOff
        ProjectFailureKind.Timeout -> Icons.Outlined.HourglassEmpty
        ProjectFailureKind.NotFound -> Icons.Outlined.ImageNotSupported
        ProjectFailureKind.Unknown -> Icons.Outlined.SearchOff
        else -> Icons.Outlined.ErrorOutline
    }
    Column(
        modifier.fillMaxSize().padding(StkDimens.SpaceBase),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(StkDimens.EmptyStateIcon).background(soft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(StkDimens.SystemFeedbackInnerIcon))
        }
        Text(title, Modifier.padding(top = StkDimens.SpaceLg), style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
        Text(message, Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary, textAlign = TextAlign.Center)
        requestId?.let { Text("请求编号：$it", Modifier.padding(top = StkDimens.SpaceLg), style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary) }
        buttonLabel?.let { StkPrimaryButton(it, onAction, Modifier.width(StkDimens.EmptyStateButtonWidth).padding(top = StkDimens.SpaceXl)) }
    }
}
