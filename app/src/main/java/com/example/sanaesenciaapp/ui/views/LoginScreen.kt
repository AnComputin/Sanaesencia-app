package com.example.sanaesenciaapp.ui.views

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

    val state by authViewModel.uiState.collectAsState()

    val app = appState

    LaunchedEffect(state.loginExitoso) {
        if (state.loginExitoso) {
            onNavigateToHome()
            authViewModel.onLoginNavegado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.inicio_btn_iniciar_sesion),
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
                    text = stringResource(R.string.inicio_login_bienvenida),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = stringResource(R.string.inicio_login_subtitulo),
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
                            text = stringResource(R.string.inicio_login_acceso),
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
                                Text(stringResource(R.string.inicio_label_correo))
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default.Email,
                                    contentDescription = stringResource(R.string.inicio_accessibility_email)
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
                                                stringResource(R.string.inicio_accessibility_ocultar_contrasena)
                                            else
                                                stringResource(R.string.inicio_accessibility_ver_contrasena)
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
                                authViewModel.iniciarSesion(appState = app)
                            },
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),

                            shape =
                                RoundedCornerShape(18.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.inicio_btn_iniciar_sesion),
                                    fontSize = 16.sp,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = stringResource(R.string.inicio_login_sin_cuenta),
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
                        text = stringResource(R.string.inicio_login_crear_cuenta),
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