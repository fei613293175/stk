package com.zzyihao.stk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.zzyihao.stk.designsystem.StkTokens

@Composable
internal fun BootstrapFailureScreen(onRetry: () -> Unit, onContinueOffline: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(StkTokens.Space24),
        verticalArrangement = Arrangement.spacedBy(StkTokens.Space16),
    ) {
        Text("暂时无法完成启动检查", style = MaterialTheme.typography.headlineSmall)
        Text("请检查网络后重试；已有有效缓存时可离线进入。", color = StkTokens.TextSecondary)
        Button(onRetry, Modifier.fillMaxWidth().testTag("bootstrap_retry")) { Text("重新检查") }
        Button(onContinueOffline, Modifier.fillMaxWidth().testTag("bootstrap_continue_offline")) { Text("离线进入") }
    }
}

@Composable
internal fun SystemFailureScreen(onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(StkTokens.Space24),
        verticalArrangement = Arrangement.spacedBy(StkTokens.Space16),
    ) {
        Text("服务暂不可用", style = MaterialTheme.typography.headlineSmall)
        Text("本次操作未提交，请稍后安全重试。", color = StkTokens.TextSecondary)
        Button(onRetry, Modifier.fillMaxWidth().testTag("system_retry")) { Text("重试") }
    }
}

@Composable
internal fun AuthRiskDialog(message: String, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = { Text("账号安全提示") },
        text = { Text(message) },
        confirmButton = {
            Button(onConfirm, Modifier.testTag("auth_risk_confirm")) { Text("我知道了") }
        },
    )
}
