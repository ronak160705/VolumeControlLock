package com.snizhy.volumecontrol.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.snizhy.volumecontrol.data.PreferencesRepository
import com.snizhy.volumecontrol.domain.VolumeManager
import kotlinx.coroutines.*

class VolumeAccessibilityService:AccessibilityService(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private lateinit var repo:PreferencesRepository; private lateinit var vm:VolumeManager
    override fun onServiceConnected(){super.onServiceConnected();repo=PreferencesRepository(this);vm=VolumeManager(this)}
    override fun onKeyEvent(event:KeyEvent):Boolean{
        if(event.action!=KeyEvent.ACTION_DOWN || (event.keyCode!=KeyEvent.KEYCODE_VOLUME_UP && event.keyCode!=KeyEvent.KEYCODE_VOLUME_DOWN)) return false
        val streamFlow=repo.prefs
        scope.launch { streamFlow.first().let{p-> if(p.locked){vm.restoreLocked(p.stream,p.lockedPercent)} } }
        return runBlocking { streamFlow.first().locked }
    }
    override fun onAccessibilityEvent(event:AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy(){scope.cancel();super.onDestroy()}
}