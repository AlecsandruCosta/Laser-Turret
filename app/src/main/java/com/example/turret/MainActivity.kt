package com.example.turret

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                TurretScreen()
            }
        }
    }
}

@Composable
private fun AngleControl(title: String, angle: Int, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text("$angle°", color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = angle.toFloat(), onValueChange = { onChange(it.toInt()) }, valueRange = 0f..180f)
    }
}

@Composable
fun TurretScreen(vm: TurretViewModel = viewModel()) {
    val pan by vm.pan.collectAsStateWithLifecycle()
    val tilt by vm.tilt.collectAsStateWithLifecycle()
    val presets by vm.presets.collectAsStateWithLifecycle()
    val ip by vm.ip.collectAsStateWithLifecycle()
    val demo by vm.demo.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Preset?>(null) }
    var name by remember { mutableStateOf("") }
    var editPan by remember { mutableIntStateOf(90) }
    var editTilt by remember { mutableIntStateOf(90) }
    var pendingDelete by remember { mutableStateOf<Preset?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("ESP32 TURRET", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Pan / Tilt Controller", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Manual control", style = MaterialTheme.typography.titleLarge)
                    AngleControl("PAN", pan, vm::setPan)
                    AngleControl("TILT", tilt, vm::setTilt)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = vm::sendPosition, modifier = Modifier.weight(1f)) { Text("SEND POSITION") }
                        OutlinedButton(onClick = { vm.setPan(90); vm.setTilt(90) }) { Text("CENTER") }
                    }
                }
            }
        }
        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Connection", style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Demo mode (without ESP32)", modifier = Modifier.weight(1f))
                        Switch(checked = demo, onCheckedChange = vm::setDemo)
                    }
                    OutlinedTextField(
                        value = ip, onValueChange = vm::setIp,
                        label = { Text("ESP32 IP / host") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        enabled = !demo,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Saved presets", style = MaterialTheme.typography.titleLarge)
                    Text("${presets.size} positions", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = { editing = null; name = ""; editPan = pan; editTilt = tilt; showEditor = true }) { Text("+ ADD") }
            }
        }
        if (presets.isEmpty()) {
            item { Text("No presets yet. Set Pan/Tilt and tap + ADD.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(presets, key = { it.id }) { preset ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(preset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Pan ${preset.pan}°  •  Tilt ${preset.tilt}°")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { vm.loadPreset(preset); vm.sendPosition() }) { Text("APPLY") }
                        TextButton(onClick = {
                            vm.loadPreset(preset)
                            editing = preset
                            name = preset.name
                            editPan = preset.pan
                            editTilt = preset.tilt
                            showEditor = true
                        }) { Text("EDIT") }
                        TextButton(onClick = { pendingDelete = preset }) { Text("DELETE") }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editing == null) "Add preset" else "Edit preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Preset name") }, singleLine = true)
                    AngleControl("PAN", editPan) { editPan = it }
                    AngleControl("TILT", editTilt) { editTilt = it }
                }
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    vm.savePreset(name, editing, editPan, editTilt)
                    showEditor = false
                }) { Text("SAVE") }
            },
            dismissButton = { TextButton(onClick = { showEditor = false }) { Text("CANCEL") } }
        )
    }
    pendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete preset?") },
            text = { Text("Delete '${preset.name}' permanently?") },
            confirmButton = { TextButton(onClick = { vm.deletePreset(preset); pendingDelete = null }) { Text("DELETE") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("CANCEL") } }
        )
    }
}
