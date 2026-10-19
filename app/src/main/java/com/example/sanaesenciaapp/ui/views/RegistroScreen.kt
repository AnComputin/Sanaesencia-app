package com.example.sanaesenciaapp.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    appState: AppState? = null,
    authViewModel: AuthViewModel = viewModel()
) {

    val state by authViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Crear una cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Completa tus datos para comenzar",
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Rol
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.rol == "PACIENTE",
                    onClick = { authViewModel.onRolChange("PACIENTE") },
                    label = { Text("Paciente") }
                )
                FilterChip(
                    selected = state.rol == "TERAPEUTA",
                    onClick = { authViewModel.onRolChange("TERAPEUTA") },
                    label = { Text("Terapeuta") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.nombre,
                onValueChange = {
                    authViewModel.onNombreChange(it)
                },
                label = {
                    Text("Nombre completo")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Nombre"
                    )
                },
                isError =
                    state.errorNombre != null,
                supportingText = {
                    AnimatedVisibility(
                        visible =
                            state.errorNombre != null
                    ) {
                        Text(
                            text =
                                state.errorNombre ?: "",
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                },

                singleLine = true,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Campo Email
            OutlinedTextField(
                value = state.email,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                isError = state.errorEmail != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorEmail != null) {
                        Text(
                            text = state.errorEmail ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo RUT
            OutlinedTextField(
                value = state.rut,
                onValueChange = { authViewModel.onRutChange(it) },
                label = { Text("RUT") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "RUT")
                },
                isError = state.errorRut != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorRut != null) {
                        Text(
                            text = state.errorRut ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Teléfono
            OutlinedTextField(
                value = state.telefono,
                onValueChange = { authViewModel.onTelefonoChange(it) },
                label = { Text("Teléfono") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Teléfono")
                },
                isError = state.errorTelefono != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorTelefono != null) {
                        Text(
                            text = state.errorTelefono ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // campo Contraseña
            OutlinedTextField(
                value = state.password,
                onValueChange = { authViewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Contraseña")
                },
                trailingIcon = {
                    IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (state.isPasswordVisible)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = "Ver contraseña"
                        )
                    }
                },
                visualTransformation = if (state.isPasswordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = state.errorPassword != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorPassword != null) {
                        Text(
                            text = state.errorPassword ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Confirmar Contraseña
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { authViewModel.onConfirmPasswordChange(it) },
                label = { Text("Confirmar contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Confirmar contraseña")
                },
                trailingIcon = {
                    IconButton(onClick = { authViewModel.toggleConfirmPasswordVisibility() }) {
                        Icon(
                            imageVector = if (state.isConfirmPasswordVisible)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = "Ver contraseña"
                        )
                    }
                },
                visualTransformation = if (state.isConfirmPasswordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = state.errorConfirmPassword != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorConfirmPassword != null) {
                        Text(
                            text = state.errorConfirmPassword ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Fecha de Nacimiento
            OutlinedTextField(
                value = state.fechaNacimiento,
                onValueChange = { authViewModel.onFechaNacimientoChange(it) },
                label = { Text("Fecha de nacimiento (DD/MM/AAAA)") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Fecha")
                },
                isError = state.errorFechaNacimiento != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorFechaNacimiento != null) {
                        Text(
                            text = state.errorFechaNacimiento ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Comuna
            OutlinedTextField(
                value = state.comuna,
                onValueChange = { authViewModel.onComunaChange(it) },
                label = { Text("Comuna") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Comuna")
                },
                isError = state.errorComuna != null,
                supportingText = {
                    AnimatedVisibility(visible = state.errorComuna != null) {
                        Text(
                            text = state.errorComuna ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox Términos y Condiciones
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.aceptaTerminos,
                    onCheckedChange = { authViewModel.onAceptarTerminosChange(it) }
                )
                Text(
                    text = "Acepto los Términos y Condiciones y la Política de Privacidad",
                    fontSize = 12.sp
                )
            }

            AnimatedVisibility(
                visible = state.errorGeneral != null
            ) {

                Text(
                    text = state.errorGeneral ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(
                        vertical = 8.dp
                    )
                )
            }

            AnimatedVisibility(visible = state.errorTerminos != null) {
                Text(
                    text = state.errorTerminos ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Registrarse
            Button(
                onClick = {
                    val valido =
                        authViewModel.validarRegistro()
                    if (valido) {
                        val nombreGenerado =
                            state.email
                                .substringBefore("@")
                                .ifEmpty { "Usuario" }
                        val registrado =
                            appState?.registrarUsuario(
                                nombre = state.nombre,
                                email = state.email,
                                password = state.password,
                                rut = state.rut,
                                telefono = state.telefono,
                                fechaNacimiento = state.fechaNacimiento,
                                comuna = state.comuna,
                                rol = state.rol
                            ) == true
                        if (registrado) {
                            authViewModel.setErrorGeneral(null)
                            onRegistroExitoso()
                        } else {
                            authViewModel.setErrorGeneral(
                                "Ya existe una cuenta con este correo"
                            )
                        }
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Registrarse")
            }
        }
    }
}
