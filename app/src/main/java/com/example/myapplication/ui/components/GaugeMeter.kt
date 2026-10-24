package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.DarkSurfaceBorder
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.ui.theme.TextSecondary

@Composable
fun GaugeMeter(
    percent: Double,
    title: String,
    subtitle: String? = null,
    size: Dp = 140.dp,
    strokeWidth: Dp = 12.dp,
    gradientColors: List<Color> = listOf(Color(0xFF0066FF), Color(0xFF00D4FF))
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0.0, 100.0).toFloat(),
        animationSpec = tween(durationMillis = 900),
        label = "GaugeAnimation"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = size.toPx()
            val radius = (canvasSize - strokePx) / 2f
            val centerOffset = androidx.compose.ui.geometry.Offset(canvasSize / 2f, canvasSize / 2f)

            // Background Track
            drawCircle(
                color = DarkSurfaceBorder.copy(alpha = 0.5f),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx)
            )

            // Progress Arc
            val sweepAngle = (animatedPercent / 100f) * 360f
            drawArc(
                brush = Brush.sweepGradient(gradientColors),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2f, strokePx / 2f),
                size = androidx.compose.ui.geometry.Size(canvasSize - strokePx, canvasSize - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedPercent.toInt()}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary.copy(alpha = 0.8f)
                )
            }
        }
    }
}
