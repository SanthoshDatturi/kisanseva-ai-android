package com.kisanseva.ai.ui.presentation.main.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanseva.ai.R

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_screen_transition")

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, delayMillis = 500),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    val animatedOffset1 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000),
            repeatMode = RepeatMode.Reverse
        ), label = "offset1"
    )

    val animatedOffset2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, delayMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "offset2"
    )

    val backgroundColor = colorResource(id = R.color.ic_launcher_background)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Abstract animated shapes
        Box(
            modifier = Modifier
                .offset(x = animatedOffset1.dp, y = (-100).dp)
                .size(200.dp)
                .scale(animatedScale)
                .alpha(animatedAlpha / 2)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-50).dp, y = animatedOffset2.dp)
                .size(150.dp)
                .scale(1.2f / animatedScale)
                .alpha(animatedAlpha)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = animatedOffset2.dp, y = 50.dp)
                .size(100.dp)
                .scale(animatedScale * 0.8f)
                .alpha(animatedAlpha)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome To",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kisan Seva AI",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}