package com.example.sanaesenciaapp.ui.views.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sanaesenciaapp.R
import com.example.sanaesenciaapp.data.repository.ResultadoBeneficiario
import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.ui.components.validationMessageText
import com.example.sanaesenciaapp.utils.validarFechaNacimiento
import com.example.sanaesenciaapp.utils.validarNombre
import com.example.sanaesenciaapp.utils.validarParentesco
import com.example.sanaesenciaapp.utils.validarRut
import com.example.sanaesenciaapp.viewmodel.BeneficiarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaBeneficiarioScreen(
    beneficiarioViewModel: BeneficiarioViewModel,
    onNavigateBack: () -> Unit
) {
    val state by beneficiarioViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var parentesco by remember { mutableStateOf("") }
    var editandoId by remember { mutableStateOf<Int?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }
    var errorParentesco by remember { mutableStateOf<String?>(null) }

    fun cargarEdicion(beneficiario: BeneficiarioEntity) {
        editandoId = beneficiario.id
        nombre = beneficiario.nombre
        rut = beneficiario.rut
        fechaNacimiento = beneficiario.fechaNacimiento
        parentesco = beneficiario.parentesco
        mensaje = null
    }

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
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.beneficiario_titulo_principal),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(stringResource(R.string.beneficiario_subt_principal))
                when {
                    state.cargando -> Text(stringResource(R.string.beneficiario_cargando))
                    state.sinSesion -> Text(stringResource(R.string.beneficiario_sesion_requerida))
                    state.errorPersistencia -> Text(
                        stringResource(R.string.beneficiario_error_generico),
                        color = MaterialTheme.colorScheme.error
                    )
                    state.beneficiarios.isEmpty() -> Text(stringResource(R.string.beneficiario_lista_vacia))
                    else -> state.beneficiarios.forEach { beneficiario ->
                        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(beneficiario.nombre, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.beneficiario_resumen_rut, beneficiario.rut))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { cargarEdicion(beneficiario) }) {
                                        Text(stringResource(R.string.beneficiario_editar))
                                    }
                                    OutlinedButton(
                                        enabled = !state.guardando,
                                        onClick = {
                                            beneficiarioViewModel.desactivar(beneficiario.id) { resultado ->
                                                mensaje = context.getString(resultado.stringId())
                                            }
                                        }
                                    ) { Text(stringResource(R.string.beneficiario_desactivar)) }
                                }
                            }
                        }
                    }
                }

                Text(
                    stringResource(if (editandoId == null) R.string.beneficiario_titulo_card else R.string.beneficiario_editar),
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(nombre, { nombre = it; errorNombre = null }, label = { Text(stringResource(R.string.beneficiario_label_nombre)) }, isError = errorNombre != null, supportingText = { errorNombre?.let { Text(validationMessageText(it)) } }, enabled = !state.guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(rut, { rut = it; errorRut = null }, label = { Text(stringResource(R.string.beneficiario_label_rut)) }, isError = errorRut != null, supportingText = { errorRut?.let { Text(validationMessageText(it)) } }, enabled = !state.guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(fechaNacimiento, { fechaNacimiento = it; errorFecha = null }, label = { Text(stringResource(R.string.beneficiario_label_fecha)) }, isError = errorFecha != null, supportingText = { errorFecha?.let { Text(validationMessageText(it)) } }, enabled = !state.guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(parentesco, { parentesco = it; errorParentesco = null }, label = { Text(stringResource(R.string.beneficiario_label_parentesco)) }, isError = errorParentesco != null, supportingText = { errorParentesco?.let { Text(validationMessageText(it)) } }, enabled = !state.guardando, modifier = Modifier.fillMaxWidth())

                mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.sinSesion) {
                    Text(stringResource(R.string.beneficiario_sesion_requerida), color = MaterialTheme.colorScheme.error)
                }

                Button(
                    enabled = !state.guardando && !state.sinSesion,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    onClick = {
                        errorNombre = validarNombre(nombre)
                        errorRut = validarRut(rut)
                        errorFecha = validarFechaNacimiento(fechaNacimiento)
                        errorParentesco = validarParentesco(parentesco)
                        if (errorNombre == null && errorRut == null && errorFecha == null && errorParentesco == null) {
                            val callback: (ResultadoBeneficiario<Nothing>) -> Unit = { resultado ->
                                mensaje = context.getString(resultado.stringId())
                                if (resultado == ResultadoBeneficiario.Insertado ||
                                    resultado == ResultadoBeneficiario.Reactivado ||
                                    resultado == ResultadoBeneficiario.Actualizado
                                ) {
                                    nombre = ""; rut = ""; fechaNacimiento = ""; parentesco = ""; editandoId = null
                                }
                            }
                            val id = editandoId
                            if (id == null) beneficiarioViewModel.registrar(nombre, rut, fechaNacimiento, parentesco, callback)
                            else beneficiarioViewModel.actualizar(id, nombre, rut, fechaNacimiento, parentesco, callback)
                        }
                    }
                ) {
                    Text(stringResource(if (state.guardando) R.string.beneficiario_btn_guardando else R.string.beneficiario_btn_guardar))
                }
                if (editandoId != null) {
                    OutlinedButton(onClick = { editandoId = null; nombre = ""; rut = ""; fechaNacimiento = ""; parentesco = "" }) {
                        Text(stringResource(R.string.beneficiario_cancelar_edicion))
                    }
                }
            }
        }
    }
}

private fun ResultadoBeneficiario<Nothing>.stringId(): Int = when (this) {
    ResultadoBeneficiario.Insertado -> R.string.beneficiario_guardado
    ResultadoBeneficiario.Reactivado -> R.string.beneficiario_reactivado
    ResultadoBeneficiario.Actualizado -> R.string.beneficiario_actualizado
    ResultadoBeneficiario.YaExisteActivo, ResultadoBeneficiario.ConflictoRut -> R.string.beneficiario_duplicado
    ResultadoBeneficiario.DatosInvalidos -> R.string.beneficiario_error_validacion
    ResultadoBeneficiario.SesionNoAutenticada -> R.string.beneficiario_sesion_requerida
    ResultadoBeneficiario.NoEncontrado -> R.string.beneficiario_no_encontrado
    is ResultadoBeneficiario.ErrorPersistencia, ResultadoBeneficiario.ErrorOperacion,
    ResultadoBeneficiario.ConflictoInsercion, ResultadoBeneficiario.InconsistenciaDatos -> R.string.beneficiario_error_generico
    ResultadoBeneficiario.Desactivado -> R.string.beneficiario_desactivado
    is ResultadoBeneficiario.Exito -> R.string.beneficiario_error_generico
}
