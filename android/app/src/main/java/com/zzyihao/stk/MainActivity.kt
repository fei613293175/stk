package com.zzyihao.stk

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zzyihao.stk.ui.StkApp
import com.zzyihao.stk.ui.system.SystemFeedbackState
import com.zzyihao.stk.ui.theme.StkTheme

class MainActivity : ComponentActivity() {
    private var startupFeedbackState by mutableStateOf<SystemFeedbackState?>(null)
    private var startupRequestId by mutableStateOf<String?>(null)
    private var deepLinkProjectId by mutableStateOf<String?>(null)
    private var deepLinkUpdateRequested by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as StkApplication).container
        readLaunchIntent(intent)
        setContent {
            StkTheme {
                StkApp(
                    container = container,
                    startupFeedbackState = startupFeedbackState,
                    startupRequestId = startupRequestId,
                    onStartupFeedbackDismiss = {
                        startupFeedbackState = null
                        startupRequestId = null
                    },
                    deepLinkProjectId = deepLinkProjectId,
                    deepLinkUpdateRequested = deepLinkUpdateRequested,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readLaunchIntent(intent)
    }

    private fun readLaunchIntent(intent: Intent) {
        startupFeedbackState = intent.getStringExtra("stk_system_state")?.let { raw ->
            runCatching { SystemFeedbackState.valueOf(raw) }.getOrNull()
        }
        startupRequestId = intent.getStringExtra("stk_request_id")?.takeIf { it.length in 1..128 }
        deepLinkProjectId = intent.data?.projectIdOrNull()
        deepLinkUpdateRequested = intent.data?.isUpdateLink() == true
    }
}

private fun Uri.isUpdateLink(): Boolean =
    scheme == "https" && host == "stk.zz-yihao.com" && pathSegments.firstOrNull() == "update"

private fun Uri.projectIdOrNull(): String? {
    if (scheme != "https" || host != "stk.zz-yihao.com") return null
    val segments = pathSegments
    val projectId = segments.getOrNull(1).takeIf { segments.getOrNull(0) == "project" } ?: return null
    return projectId.takeIf { it.matches(Regex("[A-Za-z0-9_-]{1,64}")) }
}
