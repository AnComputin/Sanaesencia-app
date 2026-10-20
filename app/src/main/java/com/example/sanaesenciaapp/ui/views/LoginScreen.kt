package com.example.sanaesenciaapp.ui.views

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    appState: AppState? = null,
    authViewModel: AuthViewModel = viewModel()
) {

    val state by authViewModel.uiState.collectAsState()

    val app = appState

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Iniciar sesión",
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
                        horizontal = 22.dp,
                        vertical = 28.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "🌿",
                    fontSize = 48.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Bienvenido nuevamente",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Ingresa a tu cuenta SanaEsencia",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                        .copy(alpha = 0.70f)
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

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
                            text = "Accede a tu cuenta",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        // Correo
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
                                    imageVector =
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
                            modifier = Modifier.height(12.dp)
                        )

                        // Contraseña
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
                                                state.isPasswordVisible
                                            )
                                                Icons.Default
                                                    .VisibilityOff
                                            else
                                                Icons.Default
                                                    .Visibility,

                                        contentDescription =
                                            if (
                                                state.isPasswordVisible
                                            )
                                                "Ocultar contraseña"
                                            else
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

                        // Error general
                        AnimatedVisibility(
                            visible =
                                state.errorGeneral != null
                        ) {

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),

                                shape =
                                    RoundedCornerShape(14.dp),

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
                                        Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(22.dp)
                        )

                        Button(
                            onClick = {

                                when {

                                    state.email.isBlank() ||
                                            state.password.isBlank() -> {

                                        authViewModel
                                            .setErrorGeneral(
                                                "Ingrese correo y contraseña"
                                            )
                                    }

                                    app == null -> {

                                        authViewModel
                                            .setErrorGeneral(
                                                "No se pudo acceder a la sesión"
                                            )
                                    }

                                    !app.existeCorreo(
                                        state.email
                                    ) -> {

                                        authViewModel
                                            .setErrorGeneral(
                                                "No existe una cuenta con este correo"
                                            )
                                    }

                                    !app.login(
                                        state.email,
                                        state.password
                                    ) -> {

                                        authViewModel
                                            .setErrorGeneral(
                                                "Contraseña incorrecta"
                                            )
                                    }

                                    else -> {

                                        authViewModel
                                            .setErrorGeneral(null)

                                        onNavigateToHome()
                                    }
                                }
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),

                            shape =
                                RoundedCornerShape(18.dp)
                        ) {

                            Text(
                                text = "Iniciar sesión",
                                fontSize = 16.sp,
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "¿Aún no tienes una cuenta?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                        .copy(alpha = 0.70f)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = onNavigateToRegistro,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                    shape =
                        RoundedCornerShape(18.dp)
                ) {

                    Text(
                        text = "Crear una cuenta",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}