package com.kisanseva.ai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    amplitudes: List<Float>
) {
    Canvas(modifier = modifier) {
        val barWidth = 2.5f
        val gap = 2f
        if (amplitudes.isEmpty()) return@Canvas
        val numBars = (size.width / (barWidth + gap)).toInt()
        val step = amplitudes.size.toFloat() / numBars

        for (i in 0 until numBars) {
            val index = (i * step).toInt().coerceIn(0, amplitudes.size - 1)
            val amplitude = amplitudes[index]
            val barHeight = amplitude * size.height
            val x = i * (barWidth + gap)
            val y = (size.height - barHeight) / 2
            drawLine(
                color = Color.Black,
                start = Offset(x, y),
                end = Offset(x, y + barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
