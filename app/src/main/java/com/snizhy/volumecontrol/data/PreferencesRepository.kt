package com.snizhy.volumecontrol.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class VolumeStream(val value: Int, val label: String) { MEDIA(3,"Media"), RING(2,"Ring"), NOTIFICATION(5,"Notification"), ALARM(4,"Alarm"), CALL(0,"Call") }

data class AppPrefs(val stream: VolumeStream=VolumeStream.MEDIA, val locked=false, val lockedPercent=60, val floating=false, val restoreOnBoot=false, val vibration=true)

private val Context.store by preferencesDataStore("volume_settings")
class PreferencesRepository(private val context: Context) {
    private object K { val stream=stringPreferencesKey("stream"); val locked=booleanPreferencesKey("locked"); val lockedPct=intPreferencesKey("locked_pct"); val floating=booleanPreferencesKey("floating"); val boot=booleanPreferencesKey("boot"); val vibration=booleanPreferencesKey("vibration") }
    val prefs: Flow<AppPrefs> = context.store.data.map { p -> AppPrefs(VolumeStream.entries.firstOrNull{it.name==p[K.stream]}?:VolumeStream.MEDIA,p[K.locked]?:false,(p[K.lockedPct]?:60).coerceIn(0,100),p[K.floating]?:false,p[K.boot]?:false,p[K.vibration]?:true) }
    suspend fun update(f:(MutablePreferences)->Unit)=context.store.edit(f)
    suspend fun setStream(s:VolumeStream)=update{it[K.stream]=s.name}
    suspend fun setLocked(v:Boolean)=update{it[K.locked]=v}
    suspend fun setLockedPercent(v:Int)=update{it[K.lockedPct]=v.coerceIn(0,100)}
    suspend fun setFloating(v:Boolean)=update{it[K.floating]=v}
    suspend fun setBoot(v:Boolean)=update{it[K.boot]=v}
    suspend fun setVibration(v:Boolean)=update{it[K.vibration]=v}
}