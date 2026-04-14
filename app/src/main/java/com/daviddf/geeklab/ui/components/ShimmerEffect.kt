package com.daviddf.geeklab.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.daviddf.geeklab.ui.theme.ShimmerColor
import com.daviddf.geeklab.ui.theme.ShimmerColorDark

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmer"
    )

    val baseColor = if (isSystemInDarkTheme()) ShimmerColorDark else ShimmerColor
    
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.6f),
                baseColor.copy(alpha = 0.2f),
                baseColor.copy(alpha = 0.6f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
    .onGloballyPositioned {
        size = it.size
    }
}
