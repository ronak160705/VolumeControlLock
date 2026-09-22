package com.snizhy.volumecontrol

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.snizhy.volumecontrol.data.VolumeStream
import com.snizhy.volumecontrol.service.FloatingVolumeService
import com.snizhy.volumecontrol.ui.MainViewModel
import com.snizhy.volumecontrol.ui.UiState

class MainActivity : ComponentActivity() {
    private val model by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    App(model)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(vm: MainViewModel) {
    val state by vm.state.collectAsState()
    var settings by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (settings) {
        SettingsScreen(state, vm) { settings = false }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volume Control & Lock") },
                actions = {
                    IconButton(onClick = { settings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(72.dp))
            Text(state.volume.toString() + "%", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(12.dp))

            Slider(
                value = state.volume.toFloat(),
                onValueChange = { vm.setVolume(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Volume " + state.volume + " percent" }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = vm::down) { Text("− Volume") }
                Button(onClick = vm::up) { Text("+ Volume") }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = vm::mute) {
                Icon(Icons.Default.VolumeOff, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Mute / Unmute")
            }

            Spacer(Modifier.height(12.dp))
            if (state.prefs.locked) {
                Button(onClick = vm::unlock) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock Volume")
                }
                Text(
                    "Volume Locked at " + state.prefs.lockedPercent + "%",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Button(onClick = vm::lock) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Lock Volume")
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Volume Type", style = MaterialTheme.typography.titleMedium)

            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(state.prefs.stream.label)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    VolumeStream.entries.forEach { stream ->
                        DropdownMenuItem(
                            text = { Text(stream.label) },
                            onClick = {
                                vm.setStream(stream)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Volume Key Control", style = MaterialTheme.typography.titleMedium)
                    Text("Enable Android Accessibility key filtering to compensate for supported physical volume-key changes while locked.")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    ) {
                        Text("Enable Volume Key Control")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Floating Controls: " + if (state.prefs.floating) "ON" else "OFF")
            Button(
                onClick = {
                    if (!Settings.canDrawOverlays(context)) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    } else {
                        context.startService(Intent(context, FloatingVolumeService::class.java))
                        vm.setFloating(true)
                    }
                }
            ) {
                Text(
                    if (Settings.canDrawOverlays(context) && state.prefs.floating) {
                        "Floating Controls ON"
                    } else {
                        "Enable Floating Controls"
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "The app works locally and does not collect contacts, location, media, microphone, camera, passwords, or analytics data.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun SettingsScreen(state: UiState, vm: MainViewModel, back: () -> Unit) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(Modifier.height(16.dp))
        Text("Volume stream: " + state.prefs.stream.label)
        Text("Locked percentage: " + state.prefs.lockedPercent + "%")
        Text("Accessibility service status can be checked in Android Settings.")
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        SettingSwitch("Floating controls", state.prefs.floating) { enabled ->
            if (!Settings.canDrawOverlays(context)) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)
                    )
                )
            } else {
                vm.setFloating(enabled)
                if (enabled) {
                    context.startService(Intent(context, FloatingVolumeService::class.java))
                } else {
                    context.stopService(Intent(context, FloatingVolumeService::class.java))
                }
            }
        }

        SettingSwitch("Start-after-reboot preference", state.prefs.restoreOnBoot, vm::setBoot)
        SettingSwitch("Vibration feedback", state.prefs.vibration, vm::setVibration)

        Spacer(Modifier.height(16.dp))
        Text("About", style = MaterialTheme.typography.titleLarge)
        Text(
            "Volume Control & Lock 1.0.0\n" +
                "Minimum Android 8.0 (API 26).\n" +
                "Android/OEM restrictions may prevent complete blocking of physical volume changes. " +
                "Accessibility filtering is used only for the volume-key feature."
        )
    }
}

@Composable
fun SettingSwitch(title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(checked = value, onCheckedChange = onChange)
    }
}