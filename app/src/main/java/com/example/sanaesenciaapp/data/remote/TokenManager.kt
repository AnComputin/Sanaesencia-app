package com.example.sanaesenciaapp.data.remote

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("sana_esencia_auth", Context.MODE_PRIVATE)

    fun guardarToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun obtenerToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun limpiarToken() {
        prefs.edit().remove("jwt_token").apply()
    }
}