package com.example.supportagent.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun GlowingOrb(
    state: OrbState = OrbState.IDLE,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AppleIntelligenceOrb")

    // Slow elegant breathing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )

    // Smooth fluid rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Secondary subtle counter-pulsing core
    val coreGlow by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CoreGlow"
    )

    // Apple Intelligence Signature Chromatic Palette
    val colors = when (state) {
        OrbState.IDLE -> listOf(
            Color(0xFF64D2FF), // Apple Cyan
            Color(0xFFBF5AF2), // Apple Purple
            Color(0xFFFF375F), // Apple Pink
            Color(0xFF5E5CE6)  // Apple Indigo
        )
        OrbState.LISTENING -> listOf(
            Color(0xFF30D158), // Apple Green
            Color(0xFF64D2FF), // Apple Cyan
            Color(0xFF0A84FF), // Apple Blue
            Color(0xFF30D158)
        )
        OrbState.THINKING -> listOf(
            Color(0xFFFF9F0A), // Apple Orange
            Color(0xFFFF375F), // Apple Pink
            Color(0xFFBF5AF2), // Apple Purple
            Color(0xFF64D2FF)
        )
        OrbState.SPEAKING -> listOf(
            Color(0xFFFF2D55), // Hot Rose
            Color(0xFFFF9F0A), // Warm Amber
            Color(0xFFBF5AF2), // Electric Purple
            Color(0xFF64D2FF)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(150.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 2.6f

            // Layer 1: Ambient Soft Glow (Expansive)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors[0].copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.8f * pulseScale
                ),
                radius = baseRadius * 1.8f * pulseScale,
                center = center
            )

            // Layer 2: Apple Intelligence Fluid Chromatic Sweep
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = colors + colors.first(),
                    center = center
                ),
                radius = baseRadius * pulseScale,
                center = center,
                alpha = 0.85f
            )

            // Layer 3: Secondary Internal Aurora Blend
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFBF5AF2).copy(alpha = 0.7f),
                        Color(0xFF64D2FF).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.1f * coreGlow
                ),
                radius = baseRadius * 1.1f * coreGlow,
                center = center
            )

            // Layer 4: Specular Pearlescent Core (White/Silver light center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFFE5E7EB).copy(alpha = 0.8f),
                        Color(0xFFBF5AF2).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.55f * coreGlow
                ),
                radius = baseRadius * 0.55f * coreGlow,
                center = center
            )
        }
    }
}
