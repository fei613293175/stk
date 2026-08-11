package com.zzyihao.stk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.MyProjectStatus
import com.zzyihao.stk.ui.main.PublishResultScreen
import com.zzyihao.stk.ui.theme.StkTheme
import org.junit.Rule
import org.junit.Test

class PublishResultInteractionTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun pendingResultUsesServerStateAndOffersAllRoutes() {
        render(MyProjectStatus.PENDING)
        composeRule.onNodeWithText("已提交审核").assertIsDisplayed()
        composeRule.onNodeWithText("查看我的发布").assertIsDisplayed()
        composeRule.onNodeWithText("继续发布").assertIsDisplayed()
    }

    @Test
    fun publishedResultUsesServerState() {
        render(MyProjectStatus.PUBLISHED)
        composeRule.onNodeWithText("发布成功").assertIsDisplayed()
        composeRule.onNodeWithText("查看项目详情").assertIsDisplayed()
        composeRule.onNodeWithText("继续发布").assertIsDisplayed()
    }

    private fun render(status: MyProjectStatus) {
        composeRule.setContent {
            StkTheme {
                PublishResultScreen(
                    project = MyProject("42", "项目标题", "项目简介", "service", "服务", status),
                    contentPadding = PaddingValues(),
                    onViewProject = {},
                    onViewMine = {},
                    onPublishAnother = {},
                )
            }
        }
    }
}
