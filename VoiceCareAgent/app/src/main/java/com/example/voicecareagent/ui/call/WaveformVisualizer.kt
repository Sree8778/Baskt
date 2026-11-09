package com.example.voicecareagent.ui.call

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.voicecareagent.model.CallStage
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isTtsSpeaking: Boolean,
    isRepSpeaking: Boolean,
    stage: CallStage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val baseColors = when {
        isTtsSpeaking -> listOf(Color(0xFF0A84FF), Color(0xFF64D2FF), Color(0xFF5E5CE6))
        isRepSpeaking -> listOf(Color(0xFFFF9F0A), Color(0xFFFF375F), Color(0xFFFF453A))
        stage == CallStage.ON_HOLD -> listOf(Color(0xFFBF5AF2), Color(0xFF5E5CE6), Color(0xFF0A84FF))
        else -> listOf(Color(0xFF30D158), Color(0xFF64D2FF), Color(0xFF0A84FF))
    }

    val amplitude = when {
        isTtsSpeaking || isRepSpeaking -> 0.85f
        stage == CallStage.ON_HOLD -> 0.40f
        else -> 0.20f
    }

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = 38
        val barSpacing = width / (barCount + 1)

        val brush = Brush.horizontalGradient(baseColors)

        for (i in 0 until barCount) {
            val x = (i + 1) * barSpacing
            val normalizedX = (i.toFloat() / barCount.toFloat()) * (2 * Math.PI).toFloat()

            val wave1 = sin(normalizedX * 2.5f + phase)
            val wave2 = sin(normalizedX * 4.0f - phase * 0.7f)
            val combined = (wave1 + wave2) / 2f

            val barHeight = (height * 0.15f) + (height * 0.35f * amplitude * (0.5f + 0.5f * combined.toFloat()))

            drawLine(
                brush = brush,
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
