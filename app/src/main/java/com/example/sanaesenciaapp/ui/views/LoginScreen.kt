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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

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

                    if (email.isBlank() || password.isBlank()) {

                        error = "Debe ingresar correo y contraseña"

                    } else {

                        error = ""

                        /*
                         * Todavía NO hacemos login real.
                         * Más adelante irá la lógica.
                         */

                    }
                },

                modifier = Modifier.fillMaxWidth()
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