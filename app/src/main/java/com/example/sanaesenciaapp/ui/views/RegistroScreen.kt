package com.example.sanaesenciaapp.ui.views

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.RutVisualTransformation
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

    val context = LocalContext.current

    var mostrarComunas by remember {
        mutableStateOf(false)
    }

    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState()

    val comunas = listOf(
        "Los Vilos",
        "Canela",
        "Illapel",
        "Salamanca",
        "La Ligua",
        "Papudo",
        "Zapallar",
        "Concón",
        "Viña del Mar",
        "Valparaíso",
        "Quilpué",
        "Villa Alemana",
        "Quillota",
        "Limache",
        "Olmué",
        "La Serena",
        "Coquimbo",
        "Ovalle",
        "Santiago",
        "Providencia",
        "Las Condes"
    )

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

                // --------------------------------
                // ENCABEZADO
                // --------------------------------

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
                            text = "Tipo de cuenta",
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
                                "Selecciona cómo utilizarás SanaEsencia.",

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
                                        .onRolChange(
                                            "TERAPEUTA"
                                        )
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
                            text = "Datos personales",
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
                                Text("Nombre completo")
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Person,

                                    contentDescription =
                                        "Nombre"
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
                                authViewModel.onRutChange(it)
                            },
                            label = { Text("RUT") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "RUT"
                                )
                            },
                            visualTransformation = RutVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii
                            ),
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
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                "Fecha de nacimiento",

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
                                    "Seleccionar fecha"
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
                                        "Seleccionar fecha"
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
                                Text("Teléfono")
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Phone,

                                    contentDescription =
                                        "Teléfono"
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
                            text = "Comuna",

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
                                        "Seleccionar comuna"
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
                                            "Seleccionar comuna"
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
                            text = "Datos de acceso",

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
                                    "Correo electrónico"
                                )
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Email,

                                    contentDescription =
                                        "Email"
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
                                Text("Contraseña")
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
                                        Icons.Default.Lock,

                                    contentDescription =
                                        "Contraseña"
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
                                                "Ocultar contraseña"
                                            else
                                                "Ver contraseña"
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
                                    "Confirmar contraseña"
                                )
                            },

                            leadingIcon = {

                                Icon(
                                    imageVector =
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
                                                "Ocultar contraseña"
                                            else
                                                "Ver contraseña"
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
                                    "Acepto los Términos y Condiciones y la Política de Privacidad",

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
                                        "No se pudo acceder a la base de datos"
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
                                            "Cuenta creada con éxito",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()

                                    onRegistroExitoso()

                                } else {

                                    authViewModel
                                        .setErrorGeneral(
                                            "Ya existe una cuenta con este correo"
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
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text =
                        "Tus datos serán utilizados únicamente dentro de este prototipo.",

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
                                "Selecciona una fecha",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {

                    Text("Aceptar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarCalendario =
                            false
                    }
                ) {

                    Text("Cancelar")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}