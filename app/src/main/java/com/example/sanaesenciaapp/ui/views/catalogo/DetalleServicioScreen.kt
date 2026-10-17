package com.example.sanaesenciaapp.ui.views.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.data.room.EspecialidadEntity
import com.example.sanaesenciaapp.ui.components.SanaButton

@Composable
fun DetalleServicioScreen(
    especialidad: EspecialidadEntity,
    onReservarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = especialidad.icono, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = especialidad.nombre, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Profesional a cargo: **${especialidad.profesional}**", fontSize = 16.sp)
            Text(text = "Modalidad: ${especialidad.modalidad}", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = especialidad.descripcion, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)

            // Aviso de Agenda Pausada (HU-02)
            if (especialidad.agendaPausada) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "⚠️ Agenda pausada temporalmente para esta especialidad. Intente más tarde o consulte por WhatsApp.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!especialidad.agendaPausada) {
            SanaButton(text = "Agendar Cita", onClick = onReservarClick)
        }
    }
}