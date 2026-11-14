package com.example.sanaesenciaapp.ui.views

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.ui.components.SanaBackground

@Composable
fun InicioScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {

    val hojaColor = MaterialTheme.colorScheme.primary

    SanaBackground {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Hoja grande decorativa detrás
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                rotate(
                    degrees = -28f,
                    pivot = Offset(
                        x = size.width * 0.76f,
                        y = size.height * 0.24f
                    )
                ) {

                    // Hoja
                    drawOval(
                        color = hojaColor.copy(alpha = 0.07f),
                        topLeft = Offset(
                            x = size.width * 0.50f,
                            y = size.height * 0.07f
                        ),
                        size = Size(
                            width = size.width * 0.42f,
                            height = size.height * 0.27f
                        )
                    )

                    // Nervadura central
                    drawLine(
                        color = hojaColor.copy(alpha = 0.10f),
                        start = Offset(
                            x = size.width * 0.71f,
                            y = size.height * 0.08f
                        ),
                        end = Offset(
                            x = size.width * 0.71f,
                            y = size.height * 0.32f
                        ),
                        strokeWidth = 4f
                    )

                    // Nervaduras laterales
                    drawLine(
                        color = hojaColor.copy(alpha = 0.055f),
                        start = Offset(
                            x = size.width * 0.71f,
                            y = size.height * 0.14f
                        ),
                        end = Offset(
                            x = size.width * 0.60f,
                            y = size.height * 0.12f
                        ),
                        strokeWidth = 3f
                    )

                    drawLine(
                        color = hojaColor.copy(alpha = 0.055f),
                        start = Offset(
                            x = size.width * 0.71f,
                            y = size.height * 0.19f
                        ),
                        end = Offset(
                            x = size.width * 0.82f,
                            y = size.height * 0.16f
                        ),
                        strokeWidth = 3f
                    )

                    drawLine(
                        color = hojaColor.copy(alpha = 0.055f),
                        start = Offset(
                            x = size.width * 0.71f,
                            y = size.height * 0.24f
                        ),
                        end = Offset(
                            x = size.width * 0.60f,
                            y = size.height * 0.22f
                        ),
                        strokeWidth = 3f
                    )
                }
            }

            // Contenido de la pantalla
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),

                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🌿",
                    fontSize = 54.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = stringResource(R.string.inicio_app_name),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = stringResource(R.string.inicio_subt_bienestar),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                        .copy(alpha = 0.75f)
                )

                Spacer(
                    modifier = Modifier.height(50.dp)
                )

                Button(
                    onClick = {
                        onLoginClick()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme.colorScheme.primary
                    )
                ) {

                    Text(
                        text = stringResource(R.string.inicio_btn_iniciar_sesion),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedButton(
                    onClick = {
                        onRegisterClick()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),

                    shape = RoundedCornerShape(18.dp)
                ) {

                    Text(
                        text = stringResource(R.string.inicio_btn_crear_cuenta),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}