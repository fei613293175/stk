package com.zzyihao.stk.ui.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zzyihao.stk.data.release.ApkUpdateManager
import com.zzyihao.stk.data.release.DownloadProgress
import com.zzyihao.stk.data.release.InstallResult
import com.zzyihao.stk.data.release.ReleaseException
import com.zzyihao.stk.data.release.ReleaseManifest
import com.zzyihao.stk.data.release.ReleaseRepository
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReleaseUpdateUiState {
    data object Checking : ReleaseUpdateUiState
    data class CheckFailed(val message: String, val requestId: String? = null) : ReleaseUpdateUiState
    data class UpToDate(val release: ReleaseManifest, val checkedAtMillis: Long) : ReleaseUpdateUiState
    data class Available(val release: ReleaseManifest, val forced: Boolean) : ReleaseUpdateUiState
    data class Downloading(
        val release: ReleaseManifest,
        val forced: Boolean,
        val progress: DownloadProgress,
    ) : ReleaseUpdateUiState
    data class DownloadFailed(val release: ReleaseManifest, val forced: Boolean, val message: String) : ReleaseUpdateUiState
    data class ReadyToInstall(val release: ReleaseManifest, val forced: Boolean, val apkPath: String) : ReleaseUpdateUiState
    data class InstallBlocked(
        val release: ReleaseManifest,
        val forced: Boolean,
        val apkPath: String,
        val message: String,
        val permissionRequired: Boolean,
    ) : ReleaseUpdateUiState
    data class InstallStarted(
        val release: ReleaseManifest,
        val forced: Boolean,
        val apkPath: String,
    ) : ReleaseUpdateUiState
}

class ReleaseUpdateViewModel(
    private val releaseRepository: ReleaseRepository,
    private val updateManager: ApkUpdateManager,
    private val installedVersionCode: Int,
    initialRelease: ReleaseManifest? = null,
    initialForced: Boolean = false,
) : ViewModel() {
    private val _state = MutableStateFlow<ReleaseUpdateUiState>(
        initialRelease?.let { ReleaseUpdateUiState.Available(it, initialForced) }
            ?: ReleaseUpdateUiState.Checking,
    )
    val state: StateFlow<ReleaseUpdateUiState> = _state.asStateFlow()
    private var downloadJob: Job? = null

    init {
        if (initialRelease == null) check()
    }

    fun check() {
        if (downloadJob?.isActive == true) return
        viewModelScope.launch {
            _state.value = ReleaseUpdateUiState.Checking
            _state.value = try {
                val release = releaseRepository.getCurrentRelease()
                val forced = installedVersionCode < release.minimumVersionCode ||
                    (release.mandatory && installedVersionCode < release.versionCode)
                if (installedVersionCode < release.versionCode) {
                    ReleaseUpdateUiState.Available(release, forced)
                } else {
                    ReleaseUpdateUiState.UpToDate(release, System.currentTimeMillis())
                }
            } catch (error: ReleaseException) {
                ReleaseUpdateUiState.CheckFailed("更新信息暂时不可用，请稍后重试。", error.requestId)
            } catch (_: Exception) {
                ReleaseUpdateUiState.CheckFailed("检查更新失败，请检查网络连接后重试。")
            }
        }
    }

    fun download() {
        val current = _state.value
        val release = when (current) {
            is ReleaseUpdateUiState.Available -> current.release
            is ReleaseUpdateUiState.DownloadFailed -> current.release
            else -> return
        }
        val forced = when (current) {
            is ReleaseUpdateUiState.Available -> current.forced
            is ReleaseUpdateUiState.DownloadFailed -> current.forced
            else -> false
        }
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _state.value = ReleaseUpdateUiState.Downloading(release, forced, DownloadProgress(0L, release.apkSizeBytes))
            try {
                val file = updateManager.download(release) { progress ->
                    _state.value = ReleaseUpdateUiState.Downloading(release, forced, progress)
                }
                _state.value = ReleaseUpdateUiState.ReadyToInstall(release, forced, file.absolutePath)
            } catch (cancelled: CancellationException) {
                _state.value = ReleaseUpdateUiState.Available(release, forced)
                throw cancelled
            } catch (error: ReleaseException) {
                _state.value = ReleaseUpdateUiState.DownloadFailed(
                    release,
                    forced,
                    error.message,
                )
            } catch (_: Exception) {
                _state.value = ReleaseUpdateUiState.DownloadFailed(
                    release,
                    forced,
                    GENERIC_DOWNLOAD_FAILURE_MESSAGE,
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
    }

    fun install() {
        val current = _state.value
        val installable = when (current) {
            is ReleaseUpdateUiState.ReadyToInstall -> Installable(current.release, current.forced, current.apkPath)
            is ReleaseUpdateUiState.InstallBlocked -> Installable(current.release, current.forced, current.apkPath)
            is ReleaseUpdateUiState.InstallStarted -> Installable(current.release, current.forced, current.apkPath)
            else -> return
        }
        when (val result = updateManager.install(File(installable.apkPath))) {
            InstallResult.Started -> _state.value = ReleaseUpdateUiState.InstallStarted(
                installable.release,
                installable.forced,
                installable.apkPath,
            )
            InstallResult.PermissionRequired -> _state.value = ReleaseUpdateUiState.InstallBlocked(
                installable.release,
                installable.forced,
                installable.apkPath,
                "请允许商推客安装未知应用，返回后继续安装。",
                permissionRequired = true,
            )
            is InstallResult.Blocked -> _state.value = ReleaseUpdateUiState.InstallBlocked(
                installable.release,
                installable.forced,
                installable.apkPath,
                result.message,
                permissionRequired = false,
            )
        }
    }

    class Factory(
        private val releaseRepository: ReleaseRepository,
        private val updateManager: ApkUpdateManager,
        private val installedVersionCode: Int,
        private val initialRelease: ReleaseManifest? = null,
        private val initialForced: Boolean = false,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ReleaseUpdateViewModel::class.java))
            return ReleaseUpdateViewModel(
                releaseRepository,
                updateManager,
                installedVersionCode,
                initialRelease,
                initialForced,
            ) as T
        }
    }

    private data class Installable(val release: ReleaseManifest, val forced: Boolean, val apkPath: String)

    companion object {
        internal const val GENERIC_DOWNLOAD_FAILURE_MESSAGE = "安装包下载失败，请检查网络连接或存储空间后重试。"
    }
}
