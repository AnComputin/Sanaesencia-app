package com.example.sanaesenciaapp.ui.views.catalogo

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.ui.components.SanaBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleServicioScreen(
    especialidad: EspecialidadEntity,
    onBackClick: () -> Unit,
    onReservarClick: () -> Unit,
) {

    val inicialesProfesional = especialidad.profesional
        .replace("Dra.", "")
        .replace("Dr.", "")
        .replace("Psic.", "")
        .replace("TO.", "")
        .replace("Klga.", "")
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.catalogo_detalle_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.beneficiario_accessibility_volver)
                        )
                    }
                }
            )
        }

    ) { padding ->

        SanaBackground {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    )
            ) {

                // Encabezado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = especialidad.icono,
                            fontSize = 34.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = especialidad.nombre,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
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
                                        stringResource(R.string.catalogo_detalle_estado_pausada)
                                    else
                                        stringResource(R.string.catalogo_detalle_estado_disponible),

                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,

                                color =
                                    if (especialidad.agendaPausada)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        MaterialTheme.colorScheme.onPrimaryContainer,

                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                )
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(26.dp)
                )

                // Profesional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer
                                ),

                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text =
                                    inicialesProfesional.ifBlank { "SE" },

                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(16.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = stringResource(R.string.catalogo_label_especialistas),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = especialidad.profesional,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),

                                color =
                                    MaterialTheme.colorScheme.primaryContainer
                            ) {

                                Text(
                                    text =
                                        stringResource(R.string.detalle_modalidad, especialidad.modalidad),

                                    fontSize = 12.sp,

                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),

                                    color =
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // Descripción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.catalogo_detalle_seccion_atencion),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = especialidad.descripcion,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.82f)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                if (especialidad.agendaPausada) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),

                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text = stringResource(R.string.catalogo_detalle_agenda_titulo),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color =
                                    MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = stringResource(R.string.catalogo_detalle_agenda_mensaje),
                                fontSize = 14.sp,
                                color =
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                } else {

                    Button(
                        onClick = onReservarClick,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                        shape = RoundedCornerShape(18.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.primary
                        )
                    ) {

                        Text(
                            text = stringResource(R.string.detalle_btn_agendar),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = stringResource(R.string.catalogo_detalle_agendar_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.60f),

                        modifier = Modifier.align(
                            Alignment.CenterHorizontally
                        )
                    )
                }

                Spacer(
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalleServicioScreenPreview() {
    DetalleServicioScreen(
        especialidad = EspecialidadEntity(
            id = 1,
            nombre = "Psicología Infanto-Juvenil",
            descripcion = "Acompañamiento psicológico integral para niños y jóvenes.",
            icono = "🌱",
            profesional = "5 Especialistas",
            modalidad = "Ambas"
        ),
        onBackClick = {},
        onReservarClick = {}
    )
}