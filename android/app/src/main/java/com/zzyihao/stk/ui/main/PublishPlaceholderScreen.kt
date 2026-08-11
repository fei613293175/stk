package com.zzyihao.stk.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

@Composable
fun PublishPlaceholderScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        StkTopBar(title = "发布")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(StkDimens.SpaceBase),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            StkInfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                    Text("发布功能开发中", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "项目发布、多图上传和审核闭环将在 V1.2.0 开放。此入口已完成，当前不会出现空白页或无响应。",
                        style = MaterialTheme.typography.bodySmall,
                        color = StkColors.TextSecondary,
                    )
                }
            }
        }
    }
}
