package com.example.sanaesenciaapp.data.remote

import android.content.Context

open class TokenManager(context: Context?) {

    private val prefs = context?.getSharedPreferences("sana_esencia_auth", Context.MODE_PRIVATE)
    private var tokenEnMemoria: String? = null

    open fun guardarToken(token: String) {
        tokenEnMemoria = token
        prefs?.edit()?.putString("jwt_token", token)?.apply()
    }

    open fun obtenerToken(): String? {
        return prefs?.getString("jwt_token", null) ?: tokenEnMemoria
    }

    open fun limpiarToken() {
        tokenEnMemoria = null
        prefs?.edit()?.remove("jwt_token")?.apply()
    }
}