package com.example.itantra.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.itantra.domain.RecordingState
import com.example.itantra.domain.ConnectionState
import com.example.itantra.theme.AlertAmber
import com.example.itantra.theme.DarkBackground
import com.example.itantra.theme.DarkSurfaceElevated
import com.example.itantra.theme.EmergencyRed
import com.example.itantra.theme.TacticalGreenLight
import com.example.itantra.theme.TextSecondary
import com.example.itantra.ui.components.AudioWaveVisualizer
import com.example.itantra.ui.components.ConnectionDialog
import com.example.itantra.ui.components.ConnectionStatusBar
import com.example.itantra.ui.components.LanguagePicker
import com.example.itantra.ui.components.MessageCard
import com.example.itantra.ui.components.PushToTalkButton

/**
 * Main walkie-talkie interface.
 *
 * Layout:
 * ┌──────────────────────────────────┐
 * │ 🟢 Connected │ Unit-Alpha │ 🚨  │  ← Status bar + Emergency toggle
 * ├──────────────────────────────────┤
 * │                                  │
 * │  [Scrollable message log]        │  ← Communication history
 * │                                  │
 * ├──────────────────────────────────┤
 * │ [Audio Waveform Visualizer]      │  ← Real-time audio level
 * │ [🌐 Language ▼] [🎤 PTT BUTTON] │  ← Controls
 * └──────────────────────────────────┘
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val audioLevel by viewModel.audioLevel.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isEmergencyMode by viewModel.isEmergencyMode.collectAsStateWithLifecycle()
    val senderId by viewModel.senderId.collectAsStateWithLifecycle()
    val isMockMode by viewModel.isMockMode.collectAsStateWithLifecycle()
    val discoveredPeers by viewModel.discoveredPeers.collectAsStateWithLifecycle()

    var showConnectionDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to newest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showConnectionDialog) {
        ConnectionDialog(
            discoveredPeers = discoveredPeers,
            isScanning = connectionState is ConnectionState.Discovering,
            onStartScanning = { viewModel.startScanning() },
            onDismiss = { showConnectionDialog = false },
            onStartServer = { viewModel.startServer() },
            onConnectToIp = { ip -> viewModel.connectToHost(ip) }
        )
    }

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (isMockMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AlertAmber.copy(alpha = 0.2f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Running in Mock Mode. Place ONNX models in assets to enable neural engine.",
                        color = AlertAmber,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Top Section: Status + Emergency ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConnectionStatusBar(
                    connectionState = connectionState,
                    senderId = senderId,
                    modifier = Modifier.weight(1f)
                )

                // Network button
                IconButton(
                    onClick = { showConnectionDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Connect",
                        tint = TacticalGreenLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Emergency toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isEmergencyMode) EmergencyRed.copy(alpha = 0.15f)
                        else DarkSurfaceElevated
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🚨", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "EMERGENCY BROADCAST",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isEmergencyMode) EmergencyRed else TextSecondary
                        )
                        Text(
                            text = if (isEmergencyMode) "MAX VOLUME • SOS ACTIVE" else "Off — normal volume",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEmergencyMode) EmergencyRed.copy(alpha = 0.7f) else TextSecondary
                        )
                    }
                }
                Switch(
                    checked = isEmergencyMode,
                    onCheckedChange = { viewModel.toggleEmergency() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmergencyRed,
                        checkedTrackColor = EmergencyRed.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkSurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Center: Message Log ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📡",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "iTantra Neural Transceiver",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TacticalGreenLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect to a peer and hold the PTT button to start communicating.\nYour speech will be transcribed and transmitted as text.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { entry ->
                            MessageCard(
                                entry = entry,
                                onReplay = { viewModel.replayMessage(entry.id) }
                            )
                        }
                    }
                }
            }

            // ── Bottom: Controls ──
            Spacer(modifier = Modifier.height(8.dp))

            // Audio waveform visualizer
            AnimatedVisibility(
                visible = recordingState == RecordingState.Recording,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AudioWaveVisualizer(
                    audioLevel = audioLevel,
                    isActive = recordingState == RecordingState.Recording,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Language picker + PTT button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LanguagePicker(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { viewModel.selectLanguage(it) }
                )

                PushToTalkButton(
                    recordingState = recordingState,
                    onPressStart = { viewModel.startRecording() },
                    onPressEnd = { viewModel.stopRecordingAndTransmit() }
                )
            }
        }
    }
}
