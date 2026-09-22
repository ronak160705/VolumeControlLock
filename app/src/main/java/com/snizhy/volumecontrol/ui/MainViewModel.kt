package com.snizhy.volumecontrol.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snizhy.volumecontrol.data.*
import com.snizhy.volumecontrol.domain.VolumeManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(val prefs:AppPrefs=AppPrefs(),val volume=0,val max=0)
class MainViewModel(app:Application):AndroidViewModel(app){
    private val repo=PreferencesRepository(app); private val vm=VolumeManager(app)
    private val refresh=MutableStateFlow(0L)
    val state=combine(repo.prefs,refresh){p,_->UiState(p,vm.percent(p.stream),vm.max(p.stream))}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),UiState())
    fun refresh(){refresh.value++}
    fun setStream(s:VolumeStream){viewModelScope.launch{repo.setStream(s)}}
    fun setVolume(p:Int){val s=state.value.prefs.stream; vm.setPercent(s,p); viewModelScope.launch{repo.setLockedPercent(p)};refresh()}
    fun up(){vm.up(state.value.prefs.stream);refresh()}
    fun down(){vm.down(state.value.prefs.stream);refresh()}
    fun mute(){vm.mute(state.value.prefs.stream);refresh()}
    fun lock(){val p=state.value.volume;viewModelScope.launch{repo.setLocked(true);repo.setLockedPercent(p)}}
    fun unlock(){viewModelScope.launch{repo.setLocked(false)}}
    fun setFloating(v:Boolean){viewModelScope.launch{repo.setFloating(v)}}
    fun setBoot(v:Boolean){viewModelScope.launch{repo.setBoot(v)}}
    fun setVibration(v:Boolean){viewModelScope.launch{repo.setVibration(v)}}
    fun restoreIfLocked(){val s=state.value;if(s.prefs.locked)vm.restoreLocked(s.prefs.stream,s.prefs.lockedPercent);refresh()}
}