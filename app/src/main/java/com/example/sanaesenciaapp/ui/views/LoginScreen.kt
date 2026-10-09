package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController,
                appState: AppState) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(
                title = {
                    Text("Iniciar sesión")
                }
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

            OutlinedTextField(
                value = email,

                onValueChange = {
                    email = it
                },

                label = {
                    Text("Correo electrónico")
                },

                singleLine = true,

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Spacer(
                modifier = Modifier.height(28.dp)
            )

            OutlinedTextField(
                value = password,

                onValueChange = {
                    password = it
                },

                label = {
                    Text("Contraseña")
                },

                singleLine = true,

                visualTransformation = PasswordVisualTransformation(),

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            if (error.isNotEmpty()) {

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            Button(

                onClick = {

                    when {

                        email.isBlank() ||
                                password.isBlank() -> {

                            error =
                                "Debe ingresar correo y contraseña"
                        }

                        password.length < 4 -> {

                            error =
                                "La contraseña debe tener al menos 4 caracteres"
                        }

                        appState.login(
                            email.trim(),
                            password
                        ) -> {

                            error = ""

                            navController.navigate("home") {

                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }

                        else -> {

                            error =
                                "Correo o contraseña incorrectos"
                        }
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text("Iniciar sesión")
            }

            TextButton(

                onClick = {
                    navController.navigate("registro")
                }

            ) {

                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}