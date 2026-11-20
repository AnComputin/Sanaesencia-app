package com.example.sanaesenciaapp.ui.views.terapeuta

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTerapeutaScreen(
    appState: AppState,
    onAgendaClick: () -> Unit,
    onPacientesClick: () -> Unit,
    onLogoutClick: () -> Unit
) {

    val usuario = appState.usuarioActual

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.terapeuta_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }
    ) { padding ->

        SanaBackground {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(R.string.terapeuta_titulo_panel),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.terapeuta_saludo, usuario?.nombre ?: stringResource(R.string.terapeuta_rol_predeterminado)),
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.terapeuta_subt_panel),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.7f
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.terapeuta_card_jornada_titulo),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.terapeuta_card_jornada_desc),
                            fontSize = 14.sp,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer
                                    .copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onAgendaClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.terapeuta_btn_agenda),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.terapeuta_card_pacientes_titulo),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.terapeuta_card_pacientes_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedButton(
                            onClick = onPacientesClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.terapeuta_btn_pacientes),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.terapeuta_btn_cerrar_sesion))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}