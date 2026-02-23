package com.kisanseva.ai.ui.presentation.main.farm.cropRecommendation.cropDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RankDisplay(rank: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fadingShadow(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = (-2).sp
        )
    }
}

/**
 * A modifier that draws a round fading shadow behind the content.
 * Useful for making items stand out against complex backgrounds.
 */
fun Modifier.fadingShadow(
    color: Color = Color.Black.copy(alpha = 0.4f),
    radius: Dp = 40.dp
) = this.drawBehind {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius.toPx()
        ),
        radius = radius.toPx(),
        center = center
    )
}
