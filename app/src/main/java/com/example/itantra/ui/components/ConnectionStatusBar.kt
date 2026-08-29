package com.example.itantra.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itantra.domain.ConnectionState
import com.example.itantra.theme.DarkSurfaceElevated
import com.example.itantra.theme.StatusConnected
import com.example.itantra.theme.StatusDisconnected
import com.example.itantra.theme.StatusListening
import com.example.itantra.theme.TacticalGreenLight
import com.example.itantra.theme.TextSecondary

/**
 * Top status bar showing P2P connection state with animated indicator dot.
 */
@Composable
fun ConnectionStatusBar(
    connectionState: ConnectionState,
    senderId: String,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (connectionState) {
            is ConnectionState.Connected -> StatusConnected
            is ConnectionState.Listening, is ConnectionState.Discovering -> StatusListening
            is ConnectionState.Connecting -> StatusListening
            is ConnectionState.Disconnected -> StatusDisconnected
            is ConnectionState.Error -> StatusDisconnected
        },
        label = "statusColor"
    )

    val statusText = when (connectionState) {
        is ConnectionState.Connected -> "Connected to ${connectionState.peerName}"
        is ConnectionState.Listening -> "Listening..."
        is ConnectionState.Discovering -> "Discovering..."
        is ConnectionState.Connecting -> "Connecting to ${connectionState.peerName}..."
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Error -> "Error: ${connectionState.message.take(30)}"
    }

    // Pulse animation for non-connected states
    val isStable = connectionState is ConnectionState.Connected ||
            connectionState is ConnectionState.Disconnected
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isStable) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }

        Text(
            text = senderId,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}
