package com.example.sanaesenciaapp.ui.views

import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.sanaesenciaapp.R

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    appState: AppState? = null,
    authViewModel: AuthViewModel = viewModel()
) {

    val state by authViewModel.uiState.collectAsState()
    val errorBaseDatos = stringResource(R.string.inicio_registro_error_bd)
    val mensajeRegistroExitoso = stringResource(R.string.inicio_registro_exito)
    val mensajeCorreoExistente = stringResource(R.string.inicio_registro_correo_existente)
    val mensajeSeleccionarFecha = stringResource(R.string.inicio_registro_seleccionar_fecha_error)

    val context = LocalContext.current

    var mostrarComunas by remember {
        mutableStateOf(false)
    }

    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState()

    val comunas = stringArrayResource(R.array.inicio_registro_comunas).toList()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(
                title = {

                    Text(
                        text = stringResource(R.string.inicio_btn_crear_cuenta),
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

                // --------------------------------
                // ENCABEZADO
                // --------------------------------

                Column {

                    Text(
                        text = stringResource(R.string.inicio_registro_titulo),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = stringResource(R.string.inicio_registro_subtitulo),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                            .copy(alpha = 0.70f)
                    )
                }

                // --------------------------------
                // TIPO DE CUENTA
                // --------------------------------

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .primaryContainer
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.inicio_registro_tipo_cuenta),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                stringResource(R.string.inicio_registro_tipo_cuenta_desc),

                            fontSize = 13.sp,

                            color =
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer
                                    .copy(alpha = 0.70f)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            FilterChip(
                                selected =
                                    state.rol == "PACIENTE",

                                onClick = {

                                    authViewModel
                                        .onRolChange(
                                            "PACIENTE"
                                        )
                                },

                                label = {
                                    Text(stringResource(R.string.inicio_registro_paciente))
                                },

                                modifier =
                                    Modifier.weight(1f)
                            )

                            FilterChip(
                                selected =
                                    state.rol == "TERAPEUTA",

                                onClick = {

                                    authViewModel
                                        .onRolChange(
                                            "TERAPEUTA"
                                        )
                                },

                                label = {
                                    Text(stringResource(R.string.inicio_registro_terapeuta))
                                },

                                modifier =
                                    Modifier.weight(1f)
                            )
                        }
                    }
                }

                // --------------------------------
                // DATOS PERSONALES
                // --------------------------------

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        ),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.inicio_registro_datos_personales),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        // -------------------------
                        // NOMBRE
                        // -------------------------

                        OutlinedTextField(
                            value = state.nombre,

                            onValueChange = {
                                authViewModel
                                    .onNombreChange(it)
                            },

                            label = {
                                Text(stringResource(R.string.inicio_registro_nombre))
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Person,

                                    contentDescription =
                                        stringResource(R.string.inicio_registro_accessibility_nombre)
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
                                            state.errorNombre
                                                ?: "",

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            },

                            singleLine = true,

                            shape =
                                RoundedCornerShape(16.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        // -------------------------
                        // RUT
                        // -------------------------

                        OutlinedTextField(
                            value = state.rut,

                            onValueChange = {
                                authViewModel
                                    .onRutChange(it)
                            },

                            label = {
                                Text(stringResource(R.string.inicio_registro_rut))
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Person,

                                    contentDescription =
                                        stringResource(R.string.inicio_registro_rut)
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
                                            state.errorRut
                                                ?: "",

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            },

                            singleLine = true,

                            shape =
                                RoundedCornerShape(16.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        // --------------------------------
                        // FECHA DE NACIMIENTO
                        // --------------------------------

                        Text(
                            text =
                                stringResource(R.string.inicio_registro_fecha_nacimiento),

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        OutlinedButton(
                            onClick = {

                                mostrarCalendario =
                                    true
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),

                            shape =
                                RoundedCornerShape(16.dp)
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.DateRange,

                                contentDescription =
                                    stringResource(R.string.inicio_registro_seleccionar_fecha)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(10.dp)
                            )

                            Text(
                                text =
                                    if (
                                        state.fechaNacimiento
                                            .isBlank()
                                    )
                                        stringResource(R.string.inicio_registro_seleccionar_fecha)
                                    else
                                        state.fechaNacimiento
                            )
                        }

                        AnimatedVisibility(
                            visible =
                                state.errorFechaNacimiento
                                        != null
                        ) {

                            Text(
                                text =
                                    state.errorFechaNacimiento
                                        ?: "",

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error,

                                fontSize = 12.sp,

                                modifier =
                                    Modifier.padding(
                                        start = 16.dp,
                                        top = 4.dp
                                    )
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        // -------------------------
                        // TELÉFONO
                        // -------------------------

                        OutlinedTextField(
                            value =
                                state.telefono,

                            onValueChange = {

                                authViewModel
                                    .onTelefonoChange(it)
                            },

                            label = {
                                Text(stringResource(R.string.inicio_registro_telefono))
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Phone,

                                    contentDescription =
                                        stringResource(R.string.inicio_registro_telefono)
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
                                            state.errorTelefono
                                                ?: "",

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            },

                            singleLine = true,

                            shape =
                                RoundedCornerShape(16.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        // --------------------------------
                        // COMUNA
                        // --------------------------------

                        Text(
                            text = stringResource(R.string.inicio_registro_comuna),

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            OutlinedButton(
                                onClick = {

                                    mostrarComunas =
                                        true
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),

                                shape =
                                    RoundedCornerShape(16.dp)
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.LocationOn,

                                    contentDescription =
                                        stringResource(R.string.inicio_registro_seleccionar_comuna)
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(10.dp)
                                )

                                Text(
                                    text =
                                        if (
                                            state.comuna
                                                .isBlank()
                                        )
                                            stringResource(R.string.inicio_registro_seleccionar_comuna)
                                        else
                                            state.comuna
                                )
                            }

                            DropdownMenu(
                                expanded =
                                    mostrarComunas,

                                onDismissRequest = {

                                    mostrarComunas =
                                        false
                                },

                                modifier =
                                    Modifier.fillMaxWidth(
                                        0.90f
                                    )
                            ) {

                                comunas.forEach {
                                        comuna ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(comuna)
                                        },

                                        onClick = {

                                            authViewModel
                                                .onComunaChange(
                                                    comuna
                                                )

                                            mostrarComunas =
                                                false
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible =
                                state.errorComuna != null
                        ) {

                            Text(
                                text =
                                    state.errorComuna ?: "",

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error,

                                fontSize = 12.sp,

                                modifier =
                                    Modifier.padding(
                                        start = 16.dp,
                                        top = 4.dp
                                    )
                            )
                        }
                    }
                }

                // --------------------------------
                // DATOS DE ACCESO
                // --------------------------------

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(22.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        ),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.inicio_registro_datos_acceso),

                            fontSize = 19.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        // -------------------------
                        // CORREO
                        // -------------------------

                        OutlinedTextField(
                            value =
                                state.email,

                            onValueChange = {

                                authViewModel
                                    .onEmailChange(it)
                            },

                            label = {
                                Text(
                                    stringResource(R.string.inicio_label_correo)
                                )
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Email,

                                    contentDescription =
                                        stringResource(R.string.inicio_accessibility_email)
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
                                            state.errorEmail
                                                ?: "",

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            },

                            singleLine = true,

                            shape =
                                RoundedCornerShape(16.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        // -------------------------
                        // CONTRASEÑA
                        // -------------------------

                        OutlinedTextField(
                            value =
                                state.password,

                            onValueChange = {

                                authViewModel
                                    .onPasswordChange(it)
                            },

                            label = {
                                Text(stringResource(R.string.inicio_label_contrasena))
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Lock,

                                    contentDescription =
                                        stringResource(R.string.inicio_accessibility_contrasena)
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
                                                state
                                                    .isPasswordVisible
                                            )
                                                Icons.Default
                                                    .VisibilityOff
                                            else
                                                Icons.Default
                                                    .Visibility,

                                        contentDescription =
                                            if (
                                                state
                                                    .isPasswordVisible
                                            )
                                                stringResource(R.string.inicio_accessibility_ocultar_contrasena)
                                            else
                                                stringResource(R.string.inicio_accessibility_ver_contrasena)
                                    )
                                }
                            },

                            visualTransformation =
                                if (
                                    state.isPasswordVisible
                                )
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
                                            state.errorPassword
                                                ?: "",

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            },

                            singleLine = true,

                            shape =
                                RoundedCornerShape(16.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        // -------------------------
                        // CONFIRMAR CONTRASEÑA
                        // -------------------------

                        OutlinedTextField(
                            value =
                                state.confirmPassword,

                            onValueChange = {

                                authViewModel
                                    .onConfirmPasswordChange(
                                        it
                                    )
                            },

                            label = {
                                Text(
                                    stringResource(R.string.inicio_registro_confirmar_contrasena)
                                )
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Lock,

                                    contentDescription =
                                        stringResource(R.string.inicio_registro_confirmar_contrasena)
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
                                                state
                                                    .isConfirmPasswordVisible
                                            )
                                                Icons.Default
                                                    .VisibilityOff
                                            else
                                                Icons.Default
                                                    .Visibility,

                                        contentDescription =
                                            if (
                                                state
                                                    .isConfirmPasswordVisible
                                            )
                                                stringResource(R.string.inicio_accessibility_ocultar_contrasena)
                                            else
                                                stringResource(R.string.inicio_accessibility_ver_contrasena)
                                    )
                                }
                            },

                            visualTransformation =
                                if (
                                    state
                                        .isConfirmPasswordVisible
                                )
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),

                            isError =
                                state.errorConfirmPassword
                                        != null,

                            supportingText = {

                                AnimatedVisibility(
                                    visible =
                                        state.errorConfirmPassword
                                                != null
                                ) {

                                    Text(
                                        text =
                                            state.errorConfirmPassword
                                                ?: "",

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                            },

                            singleLine = true,

                            shape =
                                RoundedCornerShape(16.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                }

                // --------------------------------
                // TÉRMINOS
                // --------------------------------

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(14.dp)
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
                                        .onAceptarTerminosChange(
                                            it
                                        )
                                }
                            )

                            Text(
                                text =
                                    stringResource(R.string.inicio_registro_terminos),

                                fontSize = 12.sp,

                                color =
                                    MaterialTheme
                                        .colorScheme
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
                                    MaterialTheme
                                        .colorScheme
                                        .error,

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

                // --------------------------------
                // ERROR GENERAL
                // --------------------------------

                AnimatedVisibility(
                    visible =
                        state.errorGeneral != null
                ) {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(16.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .errorContainer
                            )
                    ) {

                        Text(
                            text =
                                state.errorGeneral ?: "",

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onErrorContainer,

                            fontSize = 13.sp,

                            modifier =
                                Modifier.padding(14.dp)
                        )
                    }
                }

                // --------------------------------
                // CREAR CUENTA
                // --------------------------------

                Button(
                    onClick = {

                        val valido =
                            authViewModel
                                .validarRegistro()

                        if (valido) {

                            if (appState == null) {

                                authViewModel
                                    .setErrorGeneral(
                                        errorBaseDatos
                                    )

                            } else {

                                val registrado =
                                    appState
                                        .registrarUsuario(
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
                                        )

                                if (registrado) {

                                    authViewModel
                                        .setErrorGeneral(
                                            null
                                        )

                                    Toast
                                        .makeText(
                                            context,
                                            mensajeRegistroExitoso,
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()

                                    onRegistroExitoso()

                                } else {

                                    authViewModel
                                        .setErrorGeneral(
                                            mensajeCorreoExistente
                                        )
                                }
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
                        text = stringResource(R.string.inicio_btn_crear_cuenta),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.inicio_registro_aviso_privacidad),

                    fontSize = 12.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onBackground
                            .copy(alpha = 0.55f)
                )

                Spacer(
                    modifier =
                        Modifier.height(28.dp)
                )
            }
        }
    }

    // --------------------------------
    // CALENDARIO FECHA DE NACIMIENTO
    // --------------------------------

    if (mostrarCalendario) {

        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendario = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val fechaSeleccionada =
                            datePickerState
                                .selectedDateMillis

                        if (fechaSeleccionada != null) {

                            val formato =
                                SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                                )

                            formato.timeZone =
                                TimeZone.getTimeZone(
                                    "UTC"
                                )

                            val fecha =
                                formato.format(
                                    Date(
                                        fechaSeleccionada
                                    )
                                )

                            authViewModel
                                .onFechaNacimientoChange(
                                    fecha
                                )

                            mostrarCalendario =
                                false

                        } else {

                            Toast.makeText(
                                context,
                                mensajeSeleccionarFecha,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {

                    Text(stringResource(R.string.inicio_dialogo_aceptar))
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarCalendario =
                            false
                    }
                ) {

                    Text(stringResource(R.string.inicio_dialogo_cancelar))
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}