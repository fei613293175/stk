package com.zzyihao.stk.ui.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zzyihao.stk.data.release.ReleaseManifest
import com.zzyihao.stk.data.release.ReleaseRepository
import com.zzyihao.stk.data.release.ReleaseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StartupGate {
    data object Checking : StartupGate
    data object Continue : StartupGate
    data class Failure(
        val message: String,
        val requestId: String? = null,
    ) : StartupGate
    data class Feedback(
        val state: SystemFeedbackState,
        val release: ReleaseManifest,
        val message: String? = null,
    ) : StartupGate
}

class StartupViewModel(
    private val releaseRepository: ReleaseRepository,
    private val installedVersionCode: Int,
    private val showUpToDate: Boolean = false,
) : ViewModel() {
    private val _gate = MutableStateFlow<StartupGate>(StartupGate.Checking)
    val gate: StateFlow<StartupGate> = _gate.asStateFlow()

    init { check() }

    fun check(showRecoveredOnSuccess: Boolean = false) {
        viewModelScope.launch {
            _gate.value = StartupGate.Checking
            val release = try {
                releaseRepository.getCurrentRelease()
            } catch (error: ReleaseException) {
                _gate.value = StartupGate.Failure("启动配置暂时不可用，请稍后重试。", error.requestId)
                return@launch
            } catch (_: Exception) {
                _gate.value = StartupGate.Failure("启动配置加载失败，请检查网络连接后重试。")
                return@launch
            }
            val resolved = resolveStartupGate(
                release = release,
                installedVersionCode = installedVersionCode,
                showUpToDate = showUpToDate,
            )
            _gate.value = if (showRecoveredOnSuccess && resolved == StartupGate.Continue) {
                StartupGate.Feedback(SystemFeedbackState.ServiceRecovered, release)
            } else {
                resolved
            }
        }
    }

    fun continueToApp() { _gate.value = StartupGate.Continue }

    class Factory(
        private val releaseRepository: ReleaseRepository,
        private val installedVersionCode: Int,
        private val showUpToDate: Boolean = false,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StartupViewModel::class.java))
            return StartupViewModel(
                releaseRepository,
                installedVersionCode,
                showUpToDate,
            ) as T
        }
    }
}

internal fun resolveStartupGate(
    release: ReleaseManifest,
    installedVersionCode: Int,
    showUpToDate: Boolean = false,
): StartupGate = when {
    release.maintenance.enabled -> StartupGate.Feedback(
        SystemFeedbackState.Maintenance,
        release,
        release.maintenance.message + release.maintenance.resumeAt?.let { " 预计恢复：$it" }.orEmpty(),
    )
    installedVersionCode < release.minimumVersionCode ->
        StartupGate.Feedback(SystemFeedbackState.ForcedUpdate, release)
    release.mandatory && installedVersionCode < release.versionCode ->
        StartupGate.Feedback(SystemFeedbackState.ForcedUpdate, release)
    installedVersionCode < release.versionCode ->
        StartupGate.Feedback(SystemFeedbackState.OptionalUpdate, release)
    showUpToDate -> StartupGate.Feedback(SystemFeedbackState.UpToDate, release)
    else -> StartupGate.Continue
}
