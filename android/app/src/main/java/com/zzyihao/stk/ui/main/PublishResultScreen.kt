package com.zzyihao.stk.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.MyProjectStatus
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

@Composable
fun PublishResultScreen(
    project: MyProject,
    contentPadding: PaddingValues,
    onViewProject: () -> Unit,
    onViewMine: () -> Unit,
    onPublishAnother: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(title = "发布结果")
        Column(
            modifier = Modifier.fillMaxSize().padding(StkDimens.SpaceBase),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val isPublished = project.status == MyProjectStatus.PUBLISHED
            val isRejected = project.status == MyProjectStatus.REJECTED
            val icon: ImageVector = when {
                isPublished -> Icons.Default.CheckCircle
                isRejected -> Icons.Outlined.ErrorOutline
                else -> Icons.Outlined.WarningAmber
            }
            val iconTint = when {
                isPublished -> StkColors.Success
                isRejected -> StkColors.Error
                else -> StkColors.Warning
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(StkDimens.SystemFeedbackIcon).background(if (isPublished) StkColors.SuccessSoft else if (isRejected) StkColors.ErrorSoft else StkColors.BrandAccentSoft, CircleShape).padding(StkDimens.SpaceMd),
            )
            Text(
                text = if (isPublished) "发布成功" else if (isRejected) "发布未通过" else "已提交审核",
                style = MaterialTheme.typography.headlineSmall,
                color = StkColors.TextPrimary,
            )
            Text(
                text = if (isPublished) "项目已在首页公开展示" else if (isRejected) "请在我的发布中查看驳回原因并修改重提" else "审核结果会显示在我的发布中",
                style = MaterialTheme.typography.bodyLarge,
                color = StkColors.TextSecondary,
                modifier = Modifier.padding(top = StkDimens.SpaceSm, bottom = StkDimens.SpaceXl),
            )
            StkPrimaryButton(
                text = if (isPublished) "查看项目详情" else "查看我的发布",
                onClick = if (isPublished) onViewProject else onViewMine,
                modifier = Modifier.padding(top = StkDimens.SpaceXl),
            )
            TextButton(onClick = onPublishAnother) { Text("继续发布") }
        }
    }
}
