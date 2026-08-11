package com.zzyihao.stk.di

import android.content.Context
import com.zzyihao.stk.BuildConfig
import com.zzyihao.stk.data.auth.AuthRepository
import com.zzyihao.stk.data.auth.FakeAuthRepository
import com.zzyihao.stk.data.auth.HttpAuthRepository
import com.zzyihao.stk.data.session.TokenStore
import com.zzyihao.stk.data.project.ProjectCache
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.data.project.FakeProjectRepository
import com.zzyihao.stk.data.project.HttpProjectRepository
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import com.zzyihao.stk.data.project.FakeProjectSubmissionRepository
import com.zzyihao.stk.data.project.HttpProjectSubmissionRepository
import com.zzyihao.stk.data.account.AccountRepository
import com.zzyihao.stk.data.account.FakeAccountRepository
import com.zzyihao.stk.data.account.HttpAccountRepository
import com.zzyihao.stk.data.release.HttpReleaseRepository
import com.zzyihao.stk.data.release.ReleaseRepository
import com.zzyihao.stk.data.legal.FakeLegalRepository
import com.zzyihao.stk.data.legal.HttpLegalRepository
import com.zzyihao.stk.data.legal.LegalRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val tokenStore = TokenStore(context.applicationContext)
    private val projectCache = ProjectCache(context.applicationContext)

    val authRepository: AuthRepository = if (BuildConfig.STK_USE_FAKE_BACKEND) {
        FakeAuthRepository(tokenStore)
    } else {
        HttpAuthRepository(
            endpoint = BuildConfig.STK_API_ENDPOINT,
            tokenStore = tokenStore,
        )
    }

    val projectRepository: ProjectRepository = if (BuildConfig.STK_USE_FAKE_BACKEND) {
        FakeProjectRepository(projectCache)
    } else {
        HttpProjectRepository(BuildConfig.STK_PROJECT_API_ENDPOINT, tokenStore, projectCache)
    }

    val projectSubmissionRepository: ProjectSubmissionRepository = if (BuildConfig.STK_USE_FAKE_BACKEND) FakeProjectSubmissionRepository() else HttpProjectSubmissionRepository(BuildConfig.STK_PROJECT_API_ENDPOINT, tokenStore, context.applicationContext)
    val accountRepository: AccountRepository = if (BuildConfig.STK_USE_FAKE_BACKEND) FakeAccountRepository(tokenStore) else HttpAccountRepository(BuildConfig.STK_API_ENDPOINT, tokenStore)
    val releaseRepository: ReleaseRepository = HttpReleaseRepository(BuildConfig.STK_UPDATE_MANIFEST_URL)
    val legalRepository: LegalRepository = if (BuildConfig.STK_USE_FAKE_BACKEND) {
        FakeLegalRepository()
    } else {
        HttpLegalRepository(BuildConfig.STK_API_ENDPOINT, appContext)
    }

    fun clearCachedData() {
        projectCache.clear()
        legalRepository.clearCache()
        java.io.File(appContext.cacheDir, "project-images").deleteRecursively()
        java.io.File(appContext.cacheDir, "stk-updates").deleteRecursively()
    }
}
