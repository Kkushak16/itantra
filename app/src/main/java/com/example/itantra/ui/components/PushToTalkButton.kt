package com.example.itantra.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.itantra.domain.RecordingState
import com.example.itantra.theme.AlertAmber
import com.example.itantra.theme.EmergencyRed
import com.example.itantra.theme.TacticalGreenLight
import com.example.itantra.theme.TextSecondary

/**
 * Large Push-to-Talk button with animated pulse ring.
 *
 * States:
 * - Idle: Green outline, "HOLD TO TALK"
 * - Recording: Red fill with pulsing ring, "RECORDING..."
 * - Transcribing: Amber fill, "TRANSCRIBING..."
 * - Transmitting: Green fill, "TRANSMITTING..."
 */
@Composable
fun PushToTalkButton(
    recordingState: RecordingState,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = recordingState == RecordingState.Recording
    val isProcessing = recordingState == RecordingState.Transcribing ||
            recordingState == RecordingState.Transmitting

    val buttonColor by animateColorAsState(
        targetValue = when (recordingState) {
            RecordingState.Recording -> EmergencyRed
            RecordingState.Transcribing -> AlertAmber
            RecordingState.Transmitting -> TacticalGreenLight
            RecordingState.Idle -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "pttColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when (recordingState) {
            RecordingState.Recording -> EmergencyRed
            RecordingState.Transcribing -> AlertAmber
            RecordingState.Transmitting -> TacticalGreenLight
            RecordingState.Idle -> TacticalGreenLight
        },
        animationSpec = tween(200),
        label = "pttBorder"
    )

    val statusText = when (recordingState) {
        RecordingState.Idle -> "HOLD TO TALK"
        RecordingState.Recording -> "RECORDING..."
        RecordingState.Transcribing -> "TRANSCRIBING..."
        RecordingState.Transmitting -> "TRANSMITTING..."
    }

    // Pulse animation for recording state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isRecording) 0f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .then(
                    if (isRecording) {
                        Modifier.drawBehind {
                            drawCircle(
                                color = EmergencyRed.copy(alpha = pulseAlpha),
                                radius = size.minDimension / 2 * pulseScale
                            )
                        }
                    } else Modifier
                )
                .scale(if (isRecording) 1.05f else 1f)
                .clip(CircleShape)
                .background(buttonColor)
                .drawBehind {
                    drawCircle(
                        color = borderColor,
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
                .pointerInput(isProcessing) {
                    if (!isProcessing) {
                        detectTapGestures(
                            onPress = {
                                onPressStart()
                                tryAwaitRelease()
                                onPressEnd()
                            }
                        )
                    }
                }
        ) {
            Text(
                text = "🎤",
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = when (recordingState) {
                RecordingState.Recording -> EmergencyRed
                RecordingState.Transcribing -> AlertAmber
                RecordingState.Transmitting -> TacticalGreenLight
                RecordingState.Idle -> TextSecondary
            },
            letterSpacing = 2.sp
        )
    }
}
