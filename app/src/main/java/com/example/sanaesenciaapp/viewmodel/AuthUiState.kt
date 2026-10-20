package com.example.sanaesenciaapp.viewmodel

data class AuthUiState(

    val nombre: String = "",
    val errorNombre: String? = null,

    //email
    val email: String = "",
    val errorEmail: String? = null,

    //rut
    val rut: String = "",
    val errorRut: String? = null,

    //telefono
    val telefono: String="",
    val errorTelefono: String? = null,

    //contraseña
    val password: String = "",
    val errorPassword: String? = null,

    //confirar contraseña
    val confirmPassword: String = "",
    val errorConfirmPassword: String? = null,

    //fecha nacimiento
    val fechaNacimiento: String="",
    val errorFechaNacimiento: String? = null,

    //comuna
    val comuna: String = "Los Vilos",  //viene prellenada
    val errorComuna: String? = null,

    //Rol y termino
    val rol: String = "PACIENTE", //PACIENTE O "TERAPEUTA"
    val aceptaTerminos: Boolean = false,
    val errorTerminos: String? = null,

    //visibilidad contraseña para icono de ojo
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,

    //estado general
    val isLoading: Boolean = false,
    val errorGeneral: String? = null
)