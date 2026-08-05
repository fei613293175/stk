package com.zzyihao.stk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.zzyihao.stk.designsystem.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { StkTheme { StkApp() } } }
}

@Composable
private fun StkApp() {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(bottomBar = { NavigationBar { listOf("首页", "发布", "我的").forEachIndexed { index, label -> NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Text(if (index == 0) "⌂" else if (index == 1) "+" else "我") }, label = { Text(label) }, modifier = Modifier.testTag("INT-NAV-${index + 1}")) } } }) { padding ->
        when (tab) { 0 -> HomeScreen(Modifier.padding(padding)); 1 -> StageScreen(Modifier.padding(padding)); else -> MeScreen(Modifier.padding(padding)) }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier) { Column(modifier.fillMaxSize().padding(StkTokens.Space16)) { Text("商推客", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(StkTokens.Space16)); Text("项目推荐", style = MaterialTheme.typography.titleMedium); LazyColumn(verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { items(listOf("新媒体推广项目", "本地生活合作项目")) { title -> Card(Modifier.fillMaxWidth().testTag("HOME-PROJECT-$title")) { Column(Modifier.padding(StkTokens.Space16)) { Text(title, style = MaterialTheme.typography.titleMedium); Text("查看项目详情与联系方式", color = StkTokens.TextSecondary) } } } } } }

@Composable
private fun StageScreen(modifier: Modifier) { Column(modifier.fillMaxSize().padding(StkTokens.Space24)) { Text("项目发布", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(StkTokens.Space16)); Text("项目发布功能将在 V1.2 开放", color = StkTokens.TextSecondary); Spacer(Modifier.height(StkTokens.Space24)); Button(onClick = {}, modifier = Modifier.fillMaxWidth().testTag("INT-STAGE-001")) { Text("知道了") } } }

@Composable
private fun MeScreen(modifier: Modifier) { Column(modifier.fillMaxSize().padding(StkTokens.Space16)) { Text("我的", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(StkTokens.Space20)); Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(StkTokens.Space16)) { Text("商推客用户"); Text("UID：待登录", color = StkTokens.TextSecondary); Text("手机号：未绑定", color = StkTokens.TextSecondary) } }; Spacer(Modifier.height(StkTokens.Space16)); OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().testTag("INT-LOGOUT-001")) { Text("退出登录") } } }
