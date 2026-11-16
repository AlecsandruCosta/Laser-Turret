package com.example.turret

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class TurretViewModel(app: Application) : AndroidViewModel(app) {
    private val db = Room.databaseBuilder(app, TurretDatabase::class.java, "turret_presets.db").build()
    private val dao = db.presetDao()
    private val prefs = app.getSharedPreferences("turret_settings", 0)
    val presets = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pan = MutableStateFlow(90)
    val pan: StateFlow<Int> = _pan
    private val _tilt = MutableStateFlow(90)
    val tilt: StateFlow<Int> = _tilt
    private val _ip = MutableStateFlow(prefs.getString("ip", "192.168.4.1") ?: "192.168.4.1")
    val ip: StateFlow<String> = _ip
    private val _demo = MutableStateFlow(prefs.getBoolean("demo", true))
    val demo: StateFlow<Boolean> = _demo
    private val _message = MutableStateFlow("Demo mode: no hardware commands")
    val message: StateFlow<String> = _message

    fun setPan(value: Int) { _pan.value = value.coerceIn(0, 180) }
    fun setTilt(value: Int) { _tilt.value = value.coerceIn(0, 180) }
    fun setIp(value: String) { _ip.value = value; prefs.edit().putString("ip", value).apply() }
    fun setDemo(value: Boolean) {
        _demo.value = value
        prefs.edit().putBoolean("demo", value).apply()
        _message.value = if (value) "Demo mode: no hardware commands" else "Wi-Fi mode ready"
    }
    fun savePreset(name: String, editing: Preset?, newPan: Int, newTilt: Int) {
        val clean = name.trim()
        if (clean.isEmpty()) { _message.value = "Enter a preset name"; return }
        viewModelScope.launch {
            if (editing == null) dao.insert(Preset(name = clean, pan = newPan.coerceIn(0, 180), tilt = newTilt.coerceIn(0, 180)))
            else dao.update(editing.copy(name = clean, pan = newPan.coerceIn(0, 180), tilt = newTilt.coerceIn(0, 180)))
            _message.value = if (editing == null) "Preset saved" else "Preset updated"
        }
    }
    fun deletePreset(preset: Preset) = viewModelScope.launch {
        dao.delete(preset)
        _message.value = "Preset deleted"
    }
    fun loadPreset(preset: Preset) {
        setPan(preset.pan); setTilt(preset.tilt)
        _message.value = "Loaded: ${preset.name}"
    }
    fun sendPosition() {
        val p = pan.value; val t = tilt.value
        if (demo.value) { _message.value = "DEMO: pan=$p, tilt=$t"; return }
        val host = ip.value.trim()
        if (!Regex("^(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|[a-zA-Z0-9.-]+)$").matches(host)) {
            _message.value = "Enter a valid ESP32 host/IP"; return
        }
        _message.value = "Sending..."
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val connection = URL("http://$host/move?pan=$p&tilt=$t")
                        .openConnection() as HttpURLConnection
                    connection.connectTimeout = 2500
                    connection.readTimeout = 2500
                    connection.requestMethod = "GET"
                    try {
                        val status = connection.responseCode
                        if (status in 200..299) "Sent: pan=$p, tilt=$t" else "ESP32 HTTP error: $status"
                    } finally { connection.disconnect() }
                } catch (e: Exception) { "Connection failed: ${e.localizedMessage ?: "network error"}" }
            }
            _message.value = result
        }
    }
}
