package com.example.sanaesenciaapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun SanaBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val verde = MaterialTheme.colorScheme.primary
    val fondo = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(fondo)
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            // Mancha decorativa superior
            drawCircle(
                color = verde.copy(alpha = 0.05f),
                radius = size.width * 0.33f,
                center = Offset(
                    x = size.width * 0.97f,
                    y = size.height * 0.05f
                )
            )

            // Hojas superiores
            rotate(
                degrees = -35f,
                pivot = Offset(
                    size.width * 0.87f,
                    size.height * 0.12f
                )
            ) {
                drawOval(
                    color = verde.copy(alpha = 0.10f),
                    topLeft = Offset(
                        size.width * 0.78f,
                        size.height * 0.06f
                    ),
                    size = Size(
                        size.width * 0.13f,
                        size.height * 0.11f
                    )
                )
            }

            rotate(
                degrees = 35f,
                pivot = Offset(
                    size.width * 0.95f,
                    size.height * 0.18f
                )
            ) {
                drawOval(
                    color = verde.copy(alpha = 0.07f),
                    topLeft = Offset(
                        size.width * 0.89f,
                        size.height * 0.13f
                    ),
                    size = Size(
                        size.width * 0.12f,
                        size.height * 0.10f
                    )
                )
            }

            // Decoración inferior
            drawCircle(
                color = verde.copy(alpha = 0.04f),
                radius = size.width * 0.40f,
                center = Offset(
                    x = size.width * 0.02f,
                    y = size.height * 0.96f
                )
            )

            rotate(
                degrees = 30f,
                pivot = Offset(
                    size.width * 0.10f,
                    size.height * 0.88f
                )
            ) {
                drawOval(
                    color = verde.copy(alpha = 0.08f),
                    topLeft = Offset(
                        size.width * 0.01f,
                        size.height * 0.82f
                    ),
                    size = Size(
                        size.width * 0.16f,
                        size.height * 0.11f
                    )
                )
            }
        }

        content()
    }
}