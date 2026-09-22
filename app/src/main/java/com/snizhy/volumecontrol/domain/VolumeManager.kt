package com.snizhy.volumecontrol.domain

import android.content.Context
import android.media.AudioManager
import com.snizhy.volumecontrol.data.VolumeStream
import kotlin.math.roundToInt

class VolumeManager(context: Context) {
    private val audio=context.getSystemService(AudioManager::class.java)
    fun max(stream:VolumeStream)=runCatching{audio.getStreamMaxVolume(stream.value)}.getOrDefault(0)
    fun current(stream:VolumeStream)=runCatching{audio.getStreamVolume(stream.value)}.getOrDefault(0)
    fun percent(stream:VolumeStream):Int { val m=max(stream); return if(m<=0) 0 else ((current(stream)*100f)/m).roundToInt().coerceIn(0,100) }
    fun setPercent(stream:VolumeStream,p:Int){ val m=max(stream); if(m>0) runCatching{audio.setStreamVolume(stream.value,((p.coerceIn(0,100)/100f)*m).roundToInt(),0)} }
    fun up(stream:VolumeStream){ setPercent(stream,(percent(stream)+step(stream)).coerceAtMost(100)) }
    fun down(stream:VolumeStream){ setPercent(stream,(percent(stream)-step(stream)).coerceAtLeast(0)) }
    private fun step(s:VolumeStream)=max(s).let{if(it<=10)10 else (100f/it).roundToInt().coerceAtLeast(1)}
    fun mute(stream:VolumeStream){runCatching{audio.adjustStreamVolume(stream.value,AudioManager.ADJUST_TOGGLE_MUTE,0)}}
    fun restoreLocked(stream:VolumeStream,p:Int)=setPercent(stream,p)
}