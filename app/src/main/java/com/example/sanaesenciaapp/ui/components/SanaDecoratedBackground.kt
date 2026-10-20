package com.example.sanaesenciaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp

@Composable
fun SanaDecoratedBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Text(
            text = "🌿",
            fontSize = 120.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .alpha(0.08f)
        )

        Text(
            text = "🍃",
            fontSize = 90.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .alpha(0.06f)
        )

        Text(
            text = "🌱",
            fontSize = 70.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .alpha(0.05f)
        )

        content()
    }
}

