package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onEspecialidadClick: (Int) -> Unit
) {
    SanaBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
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
            if (cargando) item { CircularProgressIndicator() }
            else if (error) item { Text(stringResource(R.string.catalogo_error_carga), color = MaterialTheme.colorScheme.error) }
            else if (especialidades.isEmpty()) item { Text(stringResource(R.string.catalogo_vacio)) }
            else items(especialidades, key = { it.id }) { especialidad ->
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
    }
}
