package com.example.sanaesenciaapp.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    appState: AppState? = null,
    authViewModel: AuthViewModel = viewModel()
) {

    val state by authViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Bienvenido nuevamente",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Ingresa a tu cuenta SanaEsencia",
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            //campo Email
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

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Contraseña
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
            AnimatedVisibility(
                visible = state.errorGeneral != null
            ) {

                Text(
                    text = state.errorGeneral ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.errorGeneral != null) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar sesión
            Button(
                onClick = {
                    when {
                        state.email.isBlank() ||
                                state.password.isBlank() -> {

                            authViewModel.setErrorGeneral(
                                "Ingrese correo y contraseña"
                            )
                        }
                        !appState!!.existeCorreo(
                            state.email
                        ) -> {

                            authViewModel.setErrorGeneral(
                                "No existe una cuenta con este correo"
                            )
                        }
                        !appState.login(
                            state.email,
                            state.password
                        ) -> {
                            authViewModel.setErrorGeneral(
                                "Contraseña incorrecta"
                            )
                        }
                        else -> {
                            authViewModel.setErrorGeneral(null)
                            onNavigateToHome()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Iniciar sesión")
            }
        }
    }
}
