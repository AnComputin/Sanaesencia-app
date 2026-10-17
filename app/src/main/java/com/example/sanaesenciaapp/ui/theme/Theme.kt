package com.example.sanaesenciaapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SanaEsenciaColors = lightColorScheme(

    primary = VerdePrincipal,
    onPrimary = Blanco,

    secondary = VerdeOscuro,
    onSecondary = Blanco,

    background = Crema,
    onBackground = VerdeOscuro,

    surface = Blanco,
    onSurface = VerdeOscuro,

    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeOscuro,

    secondaryContainer = Beige,
    onSecondaryContainer = VerdeOscuro,

    error = ErrorRojo
)

@Composable
fun SanaEsenciaAppTheme(
    content: @Composable () -> Unit
) {

    val view = LocalView.current

    if (!view.isInEditMode) {

        val window =
            (view.context as Activity).window

        window.statusBarColor =
            Crema.toArgb()

        window.navigationBarColor =
            Crema.toArgb()

        WindowCompat.getInsetsController(
            window,
            view
        ).isAppearanceLightStatusBars = true
    }

    MaterialTheme(
        colorScheme = SanaEsenciaColors,
        typography = Typography,
        content = content
    )
}