package com.example.sanaesenciaapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.utils.esEmailValido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavController) {

    var nombre by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmarPassword by remember {
        mutableStateOf("")
    }

    var error by remember {
        mutableStateOf("")
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text("Crear cuenta")
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

            OutlinedTextField(
                value = nombre,

                onValueChange = {
                    nombre = it
                },

                label = {
                    Text("Nombre")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = email,

                onValueChange = {
                    email = it
                },

                label = {
                    Text("Correo electrónico")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = password,

                onValueChange = {
                    password = it
                },

                label = {
                    Text("Contraseña")
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = confirmarPassword,

                onValueChange = {
                    confirmarPassword = it
                },

                label = {
                    Text("Confirmar contraseña")
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(16.dp)
            )

            if (error.isNotEmpty()) {

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(
                    Modifier.height(8.dp)
                )
            }

            Button(

                onClick = {

                    when {

                        nombre.isBlank() ||
                                email.isBlank() ||
                                password.isBlank() ||
                                confirmarPassword.isBlank() -> {

                            error =
                                "Todos los campos son obligatorios"
                        }

                        !esEmailValido(email) -> {

                            error =
                                "Ingrese un correo electrónico válido"
                        }

                        password.length < 4 -> {

                            error =
                                "La contraseña debe tener al menos 4 caracteres"
                        }

                        password != confirmarPassword -> {

                            error =
                                "Las contraseñas no coinciden"
                        }

                        else -> {

                            error = ""

                            /*
                             * Registro simulado.
                             *
                             * Todavía NO guardamos el usuario.
                             */

                            navController.popBackStack()
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