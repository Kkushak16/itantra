package com.itantra.core.radio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class RadioStatus(
    val isWifiOn: Boolean,
    val isBluetoothOn: Boolean
)

@Singleton
class RadioStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _radioStatus = MutableStateFlow(checkRadioState())
    val radioStatus: StateFlow<RadioStatus> = _radioStatus.asStateFlow()

    fun checkRadioState(): RadioStatus {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val isWifiOn = wifiManager?.isWifiEnabled == true

        val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val isBluetoothOn = bluetoothManager?.adapter?.isEnabled == true

        val status = RadioStatus(isWifiOn = isWifiOn, isBluetoothOn = isBluetoothOn)
        _radioStatus.value = status
        return status
    }
}
