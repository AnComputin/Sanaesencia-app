package com.example.sanaesenciaapp.ui.views.auth

import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.utils.validarFechaNacimiento
import com.example.sanaesenciaapp.utils.validarNombre
import com.example.sanaesenciaapp.utils.validarParentesco
import com.example.sanaesenciaapp.utils.validarRut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaBeneficiarioScreen(
    appState: AppState,
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var parentesco by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }
    var errorParentesco by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    val errorGenerico = stringResource(R.string.beneficiario_error_generico)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.beneficiario_topbar),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.beneficiario_titulo_principal),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = stringResource(R.string.beneficiario_subt_principal),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.beneficiario_titulo_card),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                errorNombre = null
                                errorGeneral = null
                            },
                            label = { Text(stringResource(R.string.beneficiario_label_nombre)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            isError = errorNombre != null,
                            supportingText = {
                                AnimatedVisibility(visible = errorNombre != null) {
                                    Text(
                                        text = errorNombre ?: "",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = rut,
                            onValueChange = {
                                rut = it
                                errorRut = null
                                errorGeneral = null
                            },
                            label = { Text(stringResource(R.string.beneficiario_label_rut)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            isError = errorRut != null,
                            supportingText = {
                                AnimatedVisibility(visible = errorRut != null) {
                                    Text(
                                        text = errorRut ?: "",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = {
                                fechaNacimiento = it
                                errorFecha = null
                                errorGeneral = null
                            },
                            label = { Text(stringResource(R.string.beneficiario_label_fecha)) },
                            leadingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            },
                            isError = errorFecha != null,
                            supportingText = {
                                AnimatedVisibility(visible = errorFecha != null) {
                                    Text(
                                        text = errorFecha ?: "",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = parentesco,
                            onValueChange = {
                                parentesco = it
                                errorParentesco = null
                                errorGeneral = null
                            },
                            label = { Text(stringResource(R.string.beneficiario_label_parentesco)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            isError = errorParentesco != null,
                            supportingText = {
                                AnimatedVisibility(visible = errorParentesco != null) {
                                    Text(
                                        text = errorParentesco ?: "",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AnimatedVisibility(visible = errorGeneral != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorGeneral ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        errorNombre = validarNombre(nombre)
                        errorRut = validarRut(rut)
                        errorFecha = validarFechaNacimiento(fechaNacimiento)
                        errorParentesco = validarParentesco(parentesco)

                        val todoOk = errorNombre == null &&
                            errorRut == null &&
                            errorFecha == null &&
                            errorParentesco == null

                        if (todoOk && !guardando) {
                            guardando = true
                            appState.registrarBeneficiario(
                                nombre = nombre,
                                rut = rut,
                                fechaNacimiento = fechaNacimiento,
                                parentesco = parentesco
                            ) { exito, mensaje ->
                                guardando = false
                                if (exito) {
                                    onNavigateBack()
                                } else {
                                    errorGeneral = mensaje ?: errorGenerico
                                }
                            }
                        }
                    },
                    enabled = !guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = stringResource(if (guardando) R.string.beneficiario_btn_guardando else R.string.beneficiario_btn_guardar),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.beneficiario_aviso_asociado),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
