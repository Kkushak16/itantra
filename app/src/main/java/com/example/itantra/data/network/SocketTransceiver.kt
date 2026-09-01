package com.example.itantra.data.network

import android.util.Log
import com.example.itantra.data.model.VoicePacket
import com.example.itantra.domain.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class SocketTransceiver : P2PTransceiver {

    companion object {
        private const val TAG = "SocketTransceiver"
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<VoicePacket>(extraBufferCapacity = 10)
    override val incomingPackets: SharedFlow<VoicePacket> = _incomingPackets.asSharedFlow()

    private val _discoveredPeers = MutableStateFlow<List<PeerInfo>>(emptyList())
    override val discoveredPeers: StateFlow<List<PeerInfo>> = _discoveredPeers.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var activeSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    @Volatile
    private var isRunning = false

    private val udpBeacon = UdpBeacon()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var broadcastJob: Job? = null
    private var scanJob: Job? = null

    override suspend fun startServer(port: Int, deviceId: String) {
        withContext(Dispatchers.IO) {
            disconnect()
            try {
                _connectionState.value = ConnectionState.Listening
                serverSocket = ServerSocket(port)
                isRunning = true
                
                Log.d(TAG, "Server started, listening on port $port")
                
                // Start UDP broadcasting so clients can find us
                broadcastJob?.cancel()
                broadcastJob = scope.launch {
                    udpBeacon.startBroadcasting(deviceId, port)
                }
                
                // Accept single client for now
                val socket = serverSocket!!.accept()
                
                // Once connected, stop broadcasting
                broadcastJob?.cancel()
                udpBeacon.stopBroadcasting()
                
                activeSocket = socket
                setupStreams(socket)
                
                _connectionState.value = ConnectionState.Connected(
                    peerName = "Peer-${socket.inetAddress.hostAddress}",
                    peerAddress = socket.inetAddress.hostAddress ?: "Unknown"
                )
                
                listenForData()
            } catch (e: Exception) {
                Log.e(TAG, "Server error: ${e.message}", e)
                _connectionState.value = ConnectionState.Error("Server failed: ${e.message}")
                disconnect()
            }
        }
    }

    override suspend fun connectToHost(address: String, port: Int) {
        withContext(Dispatchers.IO) {
            stopScanning() // Stop scanning when connecting
            disconnect()
            try {
                _connectionState.value = ConnectionState.Connecting(address)
                isRunning = true
                
                val socket = Socket(address, port)
                activeSocket = socket
                setupStreams(socket)
                
                _connectionState.value = ConnectionState.Connected(
                    peerName = "Host-$address",
                    peerAddress = address
                )
                
                listenForData()
            } catch (e: Exception) {
                Log.e(TAG, "Connection error: ${e.message}", e)
                _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
                disconnect()
            }
        }
    }
    
    override suspend fun startScanning() {
        withContext(Dispatchers.IO) {
            _discoveredPeers.value = emptyList()
            if (_connectionState.value !is ConnectionState.Connected && _connectionState.value !is ConnectionState.Listening) {
                _connectionState.value = ConnectionState.Discovering
            }
            
            scanJob?.cancel()
            scanJob = scope.launch {
                udpBeacon.listenForBeacons().collect { peer ->
                    _discoveredPeers.update { current ->
                        val existing = current.find { it.deviceId == peer.deviceId }
                        if (existing == null) {
                            current + peer
                        } else {
                            current.map { if (it.deviceId == peer.deviceId) peer else it }
                        }
                    }
                }
            }
        }
    }

    override fun stopScanning() {
        scanJob?.cancel()
        if (_connectionState.value is ConnectionState.Discovering) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private fun setupStreams(socket: Socket) {
        writer = PrintWriter(socket.getOutputStream(), true)
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    }

    private suspend fun listenForData() = withContext(Dispatchers.IO) {
        try {
            while (isRunning && activeSocket?.isClosed == false) {
                val line = reader?.readLine() ?: break
                
                try {
                    val packet = Json.decodeFromString<VoicePacket>(line)
                    _incomingPackets.tryEmit(packet)
                    Log.d(TAG, "Received packet from ${packet.senderId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding packet: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Read error: ${e.message}")
        } finally {
            Log.d(TAG, "Listen loop ended")
            disconnect()
        }
    }

    override suspend fun sendPacket(packet: VoicePacket) {
        withContext(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(packet)
                writer?.println(json)
                Log.d(TAG, "Sent packet length ${json.length}")
            } catch (e: Exception) {
                Log.e(TAG, "Send error: ${e.message}")
                throw e
            }
        }
    }

    override fun disconnect() {
        isRunning = false
        broadcastJob?.cancel()
        udpBeacon.stopBroadcasting()
        
        try {
            reader?.close()
            writer?.close()
            activeSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        } finally {
            activeSocket = null
            serverSocket = null
            reader = null
            writer = null
            if (_connectionState.value !is ConnectionState.Disconnected && _connectionState.value !is ConnectionState.Error) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }
}
