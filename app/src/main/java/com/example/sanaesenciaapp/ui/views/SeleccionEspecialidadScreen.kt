package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground

@Composable
fun SeleccionEspecialidadScreen(
    navController: NavController,
    appState: AppState
) {

    SanaBackground {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Elige tu especialidad",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Selecciona el área en la que deseas recibir atención.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.7f
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            items(appState.especialidades) { especialidad ->

                Card(
                    onClick = {

                        appState.especialidadSeleccionada =
                            especialidad

                        navController.navigate(
                            "detalle_servicio"
                        )
                    },

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                text = especialidad.nombre,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(50.dp),

                                color =
                                    if (especialidad.agendaPausada)
                                        MaterialTheme.colorScheme.errorContainer
                                    else
                                        MaterialTheme.colorScheme.primaryContainer
                            ) {

                                Text(
                                    text =
                                        if (especialidad.agendaPausada)
                                            "Pausada"
                                        else
                                            "Disponible",

                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,

                                    color =
                                        if (especialidad.agendaPausada)
                                            MaterialTheme.colorScheme.onErrorContainer
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer,

                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Profesional",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = especialidad.profesional,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Modalidad",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = especialidad.modalidad,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text =
                                if (especialidad.agendaPausada)
                                    "Agenda temporalmente no disponible"
                                else
                                    "Toca para ver información y reservar",

                            fontSize = 13.sp,

                            color =
                                if (especialidad.agendaPausada)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.6f
                                    )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}