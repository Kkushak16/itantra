package com.example.itantra.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext

data class PeerInfo(
    val deviceId: String,
    val ipAddress: String,
    val tcpPort: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class UdpBeacon {

    companion object {
        private const val TAG = "UdpBeacon"
        private const val BEACON_PORT = 8889
    }

    private var broadcastSocket: DatagramSocket? = null
    private var isBroadcasting = false

    suspend fun startBroadcasting(deviceId: String, tcpPort: Int) = withContext(Dispatchers.IO) {
        isBroadcasting = true
        try {
            broadcastSocket = DatagramSocket()
            broadcastSocket?.broadcast = true
            
            val broadcastAddress = InetAddress.getByName("255.255.255.255")
            val payload = JSONObject().apply {
                put("deviceId", deviceId)
                put("tcpPort", tcpPort)
                put("type", "itantra-beacon")
            }.toString().toByteArray()

            Log.d(TAG, "Starting UDP broadcast beacon on port $BEACON_PORT")
            
            while (isBroadcasting && coroutineContext.isActive) {
                val packet = DatagramPacket(payload, payload.size, broadcastAddress, BEACON_PORT)
                broadcastSocket?.send(packet)
                delay(2000) // Broadcast every 2 seconds
            }
        } catch (e: Exception) {
            Log.e(TAG, "Broadcast error: ${e.message}")
        } finally {
            stopBroadcasting()
        }
    }

    fun stopBroadcasting() {
        isBroadcasting = false
        broadcastSocket?.close()
        broadcastSocket = null
    }

    fun listenForBeacons(): Flow<PeerInfo> = flow {
        var listenSocket: DatagramSocket? = null
        try {
            listenSocket = DatagramSocket(null).apply {
                reuseAddress = true
                bind(InetSocketAddress(BEACON_PORT))
                soTimeout = 3000
            }
            
            val buffer = ByteArray(1024)
            Log.d(TAG, "Listening for UDP beacons on port $BEACON_PORT")
            
            while (coroutineContext.isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    listenSocket.receive(packet)
                    val data = String(packet.data, 0, packet.length)
                    val json = JSONObject(data)
                    
                    if (json.optString("type") == "itantra-beacon") {
                        val peer = PeerInfo(
                            deviceId = json.getString("deviceId"),
                            ipAddress = packet.address.hostAddress ?: "",
                            tcpPort = json.getInt("tcpPort")
                        )
                        emit(peer)
                    }
                } catch (e: SocketTimeoutException) {
                    // Just loop and check coroutineContext.isActive again
                } catch (e: Exception) {
                    Log.e(TAG, "Error receiving beacon: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UDP listener: ${e.message}")
        } finally {
            listenSocket?.close()
        }
    }.flowOn(Dispatchers.IO)
}
