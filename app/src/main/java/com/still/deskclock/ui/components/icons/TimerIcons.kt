package com.still.deskclock.ui.components.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PlayIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.2f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.8f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun PauseIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val rectW = w * 0.15f
        drawRect(color, topLeft = Offset(w * 0.3f, h * 0.2f), size = Size(rectW, h * 0.6f))
        drawRect(color, topLeft = Offset(w * 0.55f, h * 0.2f), size = Size(rectW, h * 0.6f))
    }
}

@Composable
fun ResetIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Reset",
        tint = color,
        modifier = modifier.size(24.dp)
    )
}

@Composable
fun HourglassIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f, join = StrokeJoin.Round, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.15f)
            lineTo(w * 0.8f, h * 0.15f)
            lineTo(w * 0.5f, h * 0.5f)
            lineTo(w * 0.8f, h * 0.85f)
            lineTo(w * 0.2f, h * 0.85f)
            lineTo(w * 0.5f, h * 0.5f)
            close()
        }
        drawPath(path, color, style = stroke)
    }
}
