package com.snizhy.volumecontrol.service

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import com.snizhy.volumecontrol.domain.VolumeManager
import com.snizhy.volumecontrol.data.VolumeStream

class FloatingVolumeService:Service(){
    private lateinit var wm:WindowManager; private lateinit var root:LinearLayout; private lateinit var vm:VolumeManager
    override fun onCreate(){super.onCreate(); if(Build.VERSION.SDK_INT>=26)startForeground(7,notification()); vm=VolumeManager(this); wm=getSystemService(WINDOW_SERVICE) as WindowManager; create()}
    private fun notification():Notification{val ch=NotificationChannel("volume_overlay","Volume controls",NotificationManager.IMPORTANCE_LOW);getSystemService(NotificationManager::class.java).createNotificationChannel(ch);return Notification.Builder(this,"volume_overlay").setSmallIcon(com.snizhy.volumecontrol.R.drawable.ic_launcher).setContentTitle("Volume controls active").setOngoing(true).build()}
    private fun create(){if(!Settings.canDrawOverlays(this)){stopSelf();return};root=LinearLayout(this);root.orientation=LinearLayout.HORIZONTAL;root.setPadding(8,8,8,8);root.setBackgroundColor(0xEEFFFFFF.toInt());fun b(t:String,a:()->Unit){Button(this).apply{ text=t;setOnClickListener{a()};root.addView(this,LinearLayout.LayoutParams(WRAP,WRAP))}};b("−"){vm.down(VolumeStream.MEDIA)};val label=TextView(this).apply{text=" "+vm.percent(VolumeStream.MEDIA)+"% ";setTextColor(0xFF000000.toInt());textSize=16f;root.addView(this,LinearLayout.LayoutParams(WRAP,WRAP))};b("+"){vm.up(VolumeStream.MEDIA);label.text=" "+vm.percent(VolumeStream.MEDIA)+"% "};b("🔇"){vm.mute(VolumeStream.MEDIA)};b("×"){stopSelf()};val type=if(Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE;val lp=WindowManager.LayoutParams(WRAP,WRAP,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT).apply{gravity=Gravity.CENTER_VERTICAL or Gravity.END;x=8};wm.addView(root,lp)}
    override fun onStartCommand(i:Intent?,flags:Int,startId:Int)=START_NOT_STICKY
    override fun onBind(i:Intent?)=null
    override fun onDestroy(){if(::root.isInitialized)runCatching{wm.removeView(root)};super.onDestroy()}
    companion object{const val WRAP=WindowManager.LayoutParams.WRAP_CONTENT}
}