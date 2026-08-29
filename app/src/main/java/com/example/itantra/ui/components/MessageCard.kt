package com.example.itantra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.itantra.data.model.SupportedLanguage
import com.example.itantra.domain.Direction
import com.example.itantra.domain.MessageEntry
import com.example.itantra.theme.AlertAmber
import com.example.itantra.theme.DarkSurfaceCard
import com.example.itantra.theme.EmergencyCardBg
import com.example.itantra.theme.EmergencyRed
import com.example.itantra.theme.IncomingCardBg
import com.example.itantra.theme.OutgoingCardBg
import com.example.itantra.theme.TacticalGreenLight
import com.example.itantra.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Message card displaying a transcribed speech message in the communication log.
 * Differentiates incoming vs outgoing messages with distinct colors and icons.
 * Emergency messages have a red background.
 */
@Composable
fun MessageCard(
    entry: MessageEntry,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutgoing = entry.direction == Direction.OUTGOING
    val isEmergency = entry.packet.isEmergency
    val language = SupportedLanguage.fromIsoCode(entry.packet.language)

    val backgroundColor = when {
        isEmergency -> EmergencyCardBg
        isOutgoing -> OutgoingCardBg
        else -> IncomingCardBg
    }

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = timeFormat.format(Date(entry.packet.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header: Direction icon, sender, language badge, timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isOutgoing) "📤" else "📥",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOutgoing) "You" else entry.packet.senderId,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOutgoing) TacticalGreenLight else AlertAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Language badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = language?.isoCode?.uppercase() ?: entry.packet.language.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TacticalGreenLight
                        )
                    }

                    if (isEmergency) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmergencyRed)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🚨 SOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message text
            Text(
                text = entry.packet.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom: Replay button + confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replay button
                if (entry.pcmAudio != null) {
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Replay audio",
                            tint = TacticalGreenLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "conf: ${"%.0f".format(entry.packet.confidence * 100)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
