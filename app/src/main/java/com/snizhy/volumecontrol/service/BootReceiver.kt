package com.snizhy.volumecontrol.service
import android.content.*
import com.snizhy.volumecontrol.data.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){if(i.action!=Intent.ACTION_BOOT_COMPLETED)return;val p=runBlocking{PreferencesRepository(c).prefs.first()};if(p.restoreOnBoot && p.floating)runCatching{c.startForegroundService(Intent(c,FloatingVolumeService::class.java))}}}