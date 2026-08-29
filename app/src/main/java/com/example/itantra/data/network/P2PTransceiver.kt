package com.example.itantra.data.network

import com.example.itantra.data.model.VoicePacket
import com.example.itantra.domain.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface P2PTransceiver {
    val connectionState: StateFlow<ConnectionState>
    val incomingPackets: SharedFlow<VoicePacket>
    val discoveredPeers: StateFlow<List<PeerInfo>>
    
    suspend fun startServer(port: Int = 8888, deviceId: String)
    suspend fun connectToHost(address: String, port: Int = 8888)
    suspend fun sendPacket(packet: VoicePacket)
    
    suspend fun startScanning()
    fun stopScanning()
    
    fun disconnect()
}
