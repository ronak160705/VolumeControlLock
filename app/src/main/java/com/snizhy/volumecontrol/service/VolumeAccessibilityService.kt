package com.snizhy.volumecontrol.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.snizhy.volumecontrol.data.PreferencesRepository
import com.snizhy.volumecontrol.domain.VolumeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class VolumeAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repo: PreferencesRepository
    private lateinit var volumeManager: VolumeManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = PreferencesRepository(this)
        volumeManager = VolumeManager(this)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (
            event.action != KeyEvent.ACTION_DOWN ||
            (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP && event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN)
        ) return false

        val prefs = runBlocking { repo.prefs.first() }
        if (!prefs.locked) return false

        scope.launch {
            volumeManager.restoreLocked(prefs.stream, prefs.lockedPercent)
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}