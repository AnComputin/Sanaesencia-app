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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.sanaesenciaapp.ui.components.SanaBackground
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
                title = {
                    Text(
                        text = "Crear cuenta",
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
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 20.dp,
                        vertical = 22.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                // Encabezado
                Column {

                    Text(
                        text = "Únete a SanaEsencia",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Completa tus datos para crear tu cuenta.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.70f)
                    )
                }

                // Tipo de cuenta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Tipo de cuenta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = "Selecciona cómo utilizarás SanaEsencia.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.70f)
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            FilterChip(
                                selected =
                                    state.rol == "PACIENTE",

                                onClick = {
                                    authViewModel
                                        .onRolChange("PACIENTE")
                                },

                                label = {
                                    Text("Paciente")
                                },

                                modifier =
                                    Modifier.weight(1f)
                            )

                            FilterChip(
                                selected =
                                    state.rol == "TERAPEUTA",

                                onClick = {
                                    authViewModel
                                        .onRolChange("TERAPEUTA")
                                },

                                label = {
                                    Text("Terapeuta")
                                },

                                modifier =
                                    Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Datos personales
                Card(
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

                        Text(
                            text = "Datos personales",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

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
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = state.rut,

                            onValueChange = {
                                authViewModel.onRutChange(it)
                            },

                            label = {
                                Text("RUT")
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "RUT"
                                )
                            },

                            isError =
                                state.errorRut != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorRut != null
                                ) {
                                    Text(
                                        text =
                                            state.errorRut ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = state.fechaNacimiento,

                            onValueChange = {
                                authViewModel
                                    .onFechaNacimientoChange(it)
                            },

                            label = {
                                Text(
                                    "Fecha de nacimiento (DD/MM/AAAA)"
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Fecha"
                                )
                            },

                            isError =
                                state.errorFechaNacimiento != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorFechaNacimiento != null
                                ) {
                                    Text(
                                        text =
                                            state.errorFechaNacimiento ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = state.telefono,

                            onValueChange = {
                                authViewModel
                                    .onTelefonoChange(it)
                            },

                            label = {
                                Text("Teléfono")
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "Teléfono"
                                )
                            },

                            isError =
                                state.errorTelefono != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorTelefono != null
                                ) {
                                    Text(
                                        text =
                                            state.errorTelefono ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = state.comuna,

                            onValueChange = {
                                authViewModel
                                    .onComunaChange(it)
                            },

                            label = {
                                Text("Comuna")
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Comuna"
                                )
                            },

                            isError =
                                state.errorComuna != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorComuna != null
                                ) {
                                    Text(
                                        text =
                                            state.errorComuna ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Datos de acceso
                Card(
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

                        Text(
                            text = "Datos de acceso",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        OutlinedTextField(
                            value = state.email,

                            onValueChange = {
                                authViewModel.onEmailChange(it)
                            },

                            label = {
                                Text("Correo electrónico")
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email"
                                )
                            },

                            isError =
                                state.errorEmail != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorEmail != null
                                ) {
                                    Text(
                                        text =
                                            state.errorEmail ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = state.password,

                            onValueChange = {
                                authViewModel
                                    .onPasswordChange(it)
                            },

                            label = {
                                Text("Contraseña")
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Contraseña"
                                )
                            },

                            trailingIcon = {

                                IconButton(
                                    onClick = {
                                        authViewModel
                                            .togglePasswordVisibility()
                                    }
                                ) {

                                    Icon(
                                        imageVector =
                                            if (
                                                state.isPasswordVisible
                                            )
                                                Icons.Default.VisibilityOff
                                            else
                                                Icons.Default.Visibility,

                                        contentDescription =
                                            "Ver contraseña"
                                    )
                                }
                            },

                            visualTransformation =
                                if (state.isPasswordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),

                            isError =
                                state.errorPassword != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorPassword != null
                                ) {
                                    Text(
                                        text =
                                            state.errorPassword ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = state.confirmPassword,

                            onValueChange = {
                                authViewModel
                                    .onConfirmPasswordChange(it)
                            },

                            label = {
                                Text("Confirmar contraseña")
                            },

                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription =
                                        "Confirmar contraseña"
                                )
                            },

                            trailingIcon = {

                                IconButton(
                                    onClick = {
                                        authViewModel
                                            .toggleConfirmPasswordVisibility()
                                    }
                                ) {

                                    Icon(
                                        imageVector =
                                            if (
                                                state.isConfirmPasswordVisible
                                            )
                                                Icons.Default.VisibilityOff
                                            else
                                                Icons.Default.Visibility,

                                        contentDescription =
                                            "Ver contraseña"
                                    )
                                }
                            },

                            visualTransformation =
                                if (
                                    state.isConfirmPasswordVisible
                                )
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),

                            isError =
                                state.errorConfirmPassword != null,

                            supportingText = {
                                AnimatedVisibility(
                                    visible =
                                        state.errorConfirmPassword != null
                                ) {
                                    Text(
                                        text =
                                            state.errorConfirmPassword ?: "",
                                        color =
                                            MaterialTheme.colorScheme.error
                                    )
                                }
                            },

                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Términos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),

                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked =
                                    state.aceptaTerminos,

                                onCheckedChange = {
                                    authViewModel
                                        .onAceptarTerminosChange(it)
                                }
                            )

                            Text(
                                text =
                                    "Acepto los Términos y Condiciones y la Política de Privacidad",
                                fontSize = 12.sp,
                                color =
                                    MaterialTheme.colorScheme
                                        .onPrimaryContainer
                            )
                        }

                        AnimatedVisibility(
                            visible =
                                state.errorTerminos != null
                        ) {

                            Text(
                                text =
                                    state.errorTerminos ?: "",

                                color =
                                    MaterialTheme.colorScheme.error,

                                fontSize = 12.sp,

                                modifier =
                                    Modifier.padding(
                                        start = 12.dp,
                                        bottom = 6.dp
                                    )
                            )
                        }
                    }
                }

                // Error general
                AnimatedVisibility(
                    visible =
                        state.errorGeneral != null
                ) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {

                        Text(
                            text =
                                state.errorGeneral ?: "",

                            color =
                                MaterialTheme.colorScheme.onErrorContainer,

                            fontSize = 13.sp,

                            modifier =
                                Modifier.padding(14.dp)
                        )
                    }
                }

                Button(
                    onClick = {

                        val valido =
                            authViewModel.validarRegistro()

                        if (valido) {

                            val registrado =
                                appState?.registrarUsuario(
                                    nombre =
                                        state.nombre,

                                    email =
                                        state.email,

                                    password =
                                        state.password,

                                    rut =
                                        state.rut,

                                    telefono =
                                        state.telefono,

                                    fechaNacimiento =
                                        state.fechaNacimiento,

                                    comuna =
                                        state.comuna,

                                    rol =
                                        state.rol
                                ) == true

                            if (registrado) {

                                authViewModel
                                    .setErrorGeneral(null)

                                onRegistroExitoso()

                            } else {

                                authViewModel
                                    .setErrorGeneral(
                                        "Ya existe una cuenta con este correo"
                                    )
                            }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(18.dp)
                ) {

                    Text(
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tus datos serán utilizados únicamente dentro de este prototipo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                        .copy(alpha = 0.55f)
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}