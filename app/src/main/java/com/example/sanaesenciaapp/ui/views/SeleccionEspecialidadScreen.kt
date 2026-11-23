package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.R
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.ui.components.SanaBackground

@Composable
fun SeleccionEspecialidadScreen(
    especialidades: List<EspecialidadEntity>,
    cargando: Boolean,
    error: Boolean,
    onEspecialidadClick: (Int) -> Unit,
    onReintentarClick: () -> Unit = {}
) {
    SanaBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(28.dp))
                Text(
                    stringResource(R.string.catalogo_titulo_seleccionar),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.catalogo_subt_seleccionar))
                Spacer(Modifier.height(10.dp))
            }
            if (cargando) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (error) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.catalogo_error_carga),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Button(
                                onClick = onReintentarClick,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.catalogo_btn_reintentar))
                            }
                        }
                    }
                }
            } else if (especialidades.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.catalogo_vacio),
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(especialidades, key = { it.id }) { especialidad ->
                    Card(
                        onClick = { onEspecialidadClick(especialidad.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(especialidad.nombre, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(especialidad.profesional)
                            Text(especialidad.modalidad)
                            Text(
                                stringResource(
                                    if (especialidad.agendaPausada) R.string.catalogo_badge_pausada
                                    else R.string.catalogo_estado_toca_reservar
                                ),
                                color = if (especialidad.agendaPausada) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SeleccionEspecialidadPreview() {
    SeleccionEspecialidadScreen(
        especialidades = listOf(
            EspecialidadEntity(
                id = 1,
                nombre = "Psicología Infanto-Juvenil",
                descripcion = "Atención integral",
                icono = "🌱",
                profesional = "5 Especialistas",
                modalidad = "Ambas"
            )
        ),
        cargando = false,
        error = false,
        onEspecialidadClick = {}
    )
}
