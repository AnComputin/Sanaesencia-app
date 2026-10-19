package com.example.sanaesenciaapp.ui.views.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.utils.validarFechaNacimiento
import com.example.sanaesenciaapp.utils.validarNombre
import com.example.sanaesenciaapp.utils.validarParentesco
import com.example.sanaesenciaapp.utils.validarRut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaBeneficiarioScreen(
    onNavigateBack: ()-> Unit
){
    //estado local de cad campo
    var nombreMenor by remember { mutableStateOf("") }
    var errorNombre by remember { mutableStateOf<String?>(null) }

    var rutMenor by remember { mutableStateOf("") }
    var errorRut by remember { mutableStateOf<String?>(null) }

    var fechaNacimiento by remember { mutableStateOf("") }
    var errorFecha by remember { mutableStateOf<String?>(null) }

    var parentesco by remember { mutableStateOf("") }
    var errorParentesco by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Ficha Beneficiario") })
        }) {padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center) {
            Text(text = "Registrar menor a cargo",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
                )
            Text(text = "Completa los datos del menor para su atención",
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(24.dp))

            //campo nombre del menor
            OutlinedTextField(
                value = nombreMenor,
                onValueChange = {
                    nombreMenor=it
                    errorNombre= null
                },
                label = {Text("Nombre completo del menor")},
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Nombre")
                },
                isError = errorNombre !=null,
                supportingText = {
                    AnimatedVisibility(visible = errorNombre != null) {
                        Text(
                            text = errorNombre ?:"",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            //campo rut menor
            OutlinedTextField(
                value = rutMenor,
                onValueChange = {
                    rutMenor = it
                    errorRut= null
                },
                label = {Text("RUT del menor")},
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "RUT")
                },
                isError = errorRut != null,
                supportingText = {
                    AnimatedVisibility(visible = errorRut != null) {
                        Text(
                            text = errorRut ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            //campo fecha nacimiento
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = {
                    fechaNacimiento = it
                    errorFecha = null
                },
                label = {Text("Fecha de nacimiento (DD/MM/AAAA)")},
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Fecha")
                },
                isError = errorFecha != null,
                supportingText = {
                    AnimatedVisibility(visible = errorFecha != null) {
                        Text(
                            text = errorFecha?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine =  true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            //campo parenteso
            OutlinedTextField(
                value = parentesco,
                onValueChange = {
                    parentesco = it
                    errorParentesco = null
                },
                label = {Text("Parentesco (Hijo/a, Sobrino/a, Tutor/a)")},
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Parentesco")
                },
                isError = errorParentesco != null,
                supportingText = {
                    AnimatedVisibility(visible = errorParentesco != null) {
                        Text(
                            text = errorParentesco ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            //boton guardar
            Button(
                onClick = {
                    errorNombre = validarNombre(nombreMenor)
                    errorRut = validarRut(rutMenor)
                    errorFecha = validarFechaNacimiento(fechaNacimiento)
                    errorParentesco = validarParentesco(parentesco)

                    //si todo esta bien, regresar
                    val todoOk = errorNombre == null
                            &&errorRut == null
                            &&errorFecha == null
                            &&errorParentesco == null

                    if (todoOk){
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar ficha")
            }
        }
    }
}