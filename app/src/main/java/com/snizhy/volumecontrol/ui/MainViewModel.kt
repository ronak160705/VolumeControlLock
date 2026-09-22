package com.snizhy.volumecontrol.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snizhy.volumecontrol.data.AppPrefs
import com.snizhy.volumecontrol.data.PreferencesRepository
import com.snizhy.volumecontrol.data.VolumeStream
import com.snizhy.volumecontrol.domain.VolumeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val prefs: AppPrefs = AppPrefs(),
    val volume: Int = 0,
    val max: Int = 0
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PreferencesRepository(app)
    private val volumeManager = VolumeManager(app)
    private val refresh = MutableStateFlow(0L)

    val state: StateFlow<UiState> = combine(repo.prefs, refresh) { prefs, _ ->
        UiState(prefs, volumeManager.percent(prefs.stream), volumeManager.max(prefs.stream))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun refresh() {
        refresh.value++
    }

    fun setStream(stream: VolumeStream) {
        viewModelScope.launch { repo.setStream(stream) }
    }

    fun setVolume(percent: Int) {
        val stream = state.value.prefs.stream
        volumeManager.setPercent(stream, percent)
        viewModelScope.launch { repo.setLockedPercent(percent) }
        refresh()
    }

    fun up() {
        volumeManager.up(state.value.prefs.stream)
        refresh()
    }

    fun down() {
        volumeManager.down(state.value.prefs.stream)
        refresh()
    }

    fun mute() {
        volumeManager.mute(state.value.prefs.stream)
        refresh()
    }

    fun lock() {
        val percent = state.value.volume
        viewModelScope.launch {
            repo.setLocked(true)
            repo.setLockedPercent(percent)
        }
    }

    fun unlock() {
        viewModelScope.launch { repo.setLocked(false) }
    }

    fun setFloating(enabled: Boolean) {
        viewModelScope.launch { repo.setFloating(enabled) }
    }

    fun setBoot(enabled: Boolean) {
        viewModelScope.launch { repo.setBoot(enabled) }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch { repo.setVibration(enabled) }
    }
}