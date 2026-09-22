package com.snizhy.volumecontrol

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.snizhy.volumecontrol.data.VolumeStream
import com.snizhy.volumecontrol.service.FloatingVolumeService
import com.snizhy.volumecontrol.ui.MainViewModel

class MainActivity:ComponentActivity(){
 private val model by viewModels<MainViewModel>()
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MaterialTheme{Surface{App(model)}}}}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(vm:MainViewModel){
 val s by vm.state.collectAsState(); var settings by remember{mutableStateOf(false)}; val ctx=LocalContext.current
 if(settings) SettingsScreen(s,vm,{settings=false}) else Scaffold(topBar={TopAppBar(title={Text("Volume Control & Lock")},actions={IconButton({settings=true}){Icon(Icons.Default.Settings,"Settings")}})}){pad->Column(Modifier.padding(pad).padding(20.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally){
  Icon(Icons.Default.VolumeUp,null,Modifier.size(72.dp)); Text(s.volume.toString()+"%",style=MaterialTheme.typography.displayMedium);Spacer(Modifier.height(12.dp))
  Slider(value=s.volume.toFloat(),onValueChange={vm.setVolume(it.toInt())},valueRange=0f..100f,modifier=Modifier.fillMaxWidth().semantics{contentDescription="Volume "+s.volume+" percent"})
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){Button({vm.down()}){Text("− Volume")};Button({vm.up()}){Text("+ Volume")}}
  Spacer(Modifier.height(8.dp));OutlinedButton({vm.mute()}){Icon(Icons.Default.VolumeOff,null);Spacer(Modifier.width(8.dp));Text("Mute / Unmute")}
  Spacer(Modifier.height(12.dp));if(s.prefs.locked){Button({vm.unlock()}){Icon(Icons.Default.LockOpen,null);Spacer(Modifier.width(8.dp));Text("Unlock Volume")};Text("Volume Locked at "+s.prefs.lockedPercent+"%",style=MaterialTheme.typography.titleMedium)}else{Button({vm.lock()}){Icon(Icons.Default.Lock,null);Spacer(Modifier.width(8.dp));Text("Lock Volume")}}
  Spacer(Modifier.height(20.dp));Text("Volume Type",style=MaterialTheme.typography.titleMedium);var expanded by remember{mutableStateOf(false)};Box{OutlinedButton({expanded=true}){Text(s.prefs.stream.label)};DropdownMenu(expanded,{expanded=false}){VolumeStream.entries.forEach{DropdownMenuItem(text={Text(it.label)},onClick={vm.setStream(it);expanded=false})}}}
  Spacer(Modifier.height(20.dp));Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Volume Key Control",style=MaterialTheme.typography.titleMedium);Text("Enable Android Accessibility key filtering to compensate for supported physical volume-key changes while locked.");Spacer(Modifier.height(8.dp));Button({ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}){Text("Enable Volume Key Control")}}
  }
  Spacer(Modifier.height(12.dp));Text("Floating Controls: "+if(s.prefs.floating)"ON" else "OFF");Button({if(!Settings.canDrawOverlays(ctx))ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+ctx.packageName))) else {ctx.startService(Intent(ctx,FloatingVolumeService::class.java));vm.setFloating(true)}}){Text(if(Settings.canDrawOverlays(ctx)&&s.prefs.floating)"Floating Controls ON" else "Enable Floating Controls")}
  Spacer(Modifier.height(16.dp));Text("The app works locally and does not collect contacts, location, media, microphone, camera, passwords, or analytics data.",style=MaterialTheme.typography.bodySmall)
 }} }
}

@Composable fun SettingsScreen(s:com.snizhy.volumecontrol.ui.UiState,vm:MainViewModel,back:()->Unit){val ctx=LocalContext.current;Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton(back){Icon(Icons.Default.ArrowBack,"Back")};Text("Settings",style=MaterialTheme.typography.headlineSmall)};Spacer(Modifier.height(16.dp));Text("Volume stream: "+s.prefs.stream.label);Text("Locked percentage: "+s.prefs.lockedPercent+"%");Text("Accessibility service status can be checked in Android Settings.");HorizontalDivider(Modifier.padding(vertical=12.dp));SettingSwitch("Floating controls",s.prefs.floating){if(!Settings.canDrawOverlays(ctx))ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+ctx.packageName))) else {vm.setFloating(it);if(it)ctx.startService(Intent(ctx,FloatingVolumeService::class.java)) else ctx.stopService(Intent(ctx,FloatingVolumeService::class.java))}};SettingSwitch("Start-after-reboot preference",s.prefs.restoreOnBoot,vm::setBoot);SettingSwitch("Vibration feedback",s.prefs.vibration,vm::setVibration);Spacer(Modifier.height(16.dp));Text("About",style=MaterialTheme.typography.titleLarge);Text("Volume Control & Lock 1.0.0\nMinimum Android 8.0 (API 26).\nAndroid/OEM restrictions may prevent complete blocking of physical volume changes. Accessibility filtering is used only for the volume-key feature.");}
}
@Composable fun SettingSwitch(t:String,v:Boolean,on:(Boolean)->Unit){Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text(t);Switch(checked=v,onCheckedChange=on)}}