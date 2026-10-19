package com.example.sanaesenciaapp.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

object CalendarioHelper {

    fun agregarACalendario(
        context: Context,
        titulo: String,
        descripcion: String,
        inicioMillis: Long,
        duracionMinutos: Int = 50,
        ubicacion: String = ""
    ): Boolean {
        val finMillis = inicioMillis + (duracionMinutos * 60 * 1000L)
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, finMillis)
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
            putExtra(CalendarContract.Events.EVENT_LOCATION, ubicacion)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
