package com.example.itantra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.itantra.theme.DarkSurface
import com.example.itantra.theme.DarkSurfaceElevated
import com.example.itantra.theme.TacticalGreenLight
import com.example.itantra.theme.TextPrimary
import com.example.itantra.theme.TextSecondary

/**
 * Connection dialog for Wi-Fi Direct peer selection or manual IP entry.
 * Two modes:
 * - Host: Start as server (listening for connections)
 * - Join: Connect to a specific IP address
 */
import com.example.itantra.data.network.PeerInfo

@Composable
fun ConnectionDialog(
    discoveredPeers: List<PeerInfo>,
    isScanning: Boolean,
    onStartScanning: () -> Unit,
    onDismiss: () -> Unit,
    onStartServer: () -> Unit,
    onConnectToIp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8888") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📡 P2P Connection",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TacticalGreenLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect over Wi-Fi Direct or Local Network",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option 1: Host (Start Server)
                Button(
                    onClick = {
                        onStartServer()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalGreenLight.copy(alpha = 0.15f),
                        contentColor = TacticalGreenLight
                    )
                ) {
                    Text(
                        text = "🖥️  HOST — Start Server",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(DarkSurfaceElevated)
                    )
                    Text(
                        text = "  OR  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(DarkSurfaceElevated)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1.5: Scan & Pair (Auto Discovery)
                Button(
                    onClick = onStartScanning,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isScanning,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalGreenLight,
                        contentColor = DarkSurface
                    )
                ) {
                    Text(
                        text = if (isScanning) "Searching for peers..." else "🔍 SCAN & PAIR",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (discoveredPeers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    discoveredPeers.forEach { peer ->
                        OutlinedButton(
                            onClick = { 
                                onConnectToIp(peer.ipAddress)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Connect to ${peer.deviceId} (${peer.ipAddress})")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(DarkSurfaceElevated)
                    )
                    Text(
                        text = "  OR  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(DarkSurfaceElevated)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "JOIN — Enter Host IP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Address") },
                        placeholder = { Text("192.168.49.1") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TacticalGreenLight,
                            cursorColor = TacticalGreenLight,
                            focusedLabelColor = TacticalGreenLight
                        )
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TacticalGreenLight,
                            cursorColor = TacticalGreenLight,
                            focusedLabelColor = TacticalGreenLight
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (ipAddress.isNotBlank()) {
                            onConnectToIp(ipAddress)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ipAddress.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalGreenLight
                    )
                ) {
                    Text(
                        text = "CONNECT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
