package com.example.sanaesenciaapp.ui.views.juegos

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
private fun JuegosHubScreenPreview() {
    JuegosHubScreen(
        onBackClick = {},
        onRespiracionGuiadaClick = {},
        onPausaConscienteClick = {},
        onMemoriceClick = {},
        onGatoClick = {},
        onCaminoBienestarClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun RespiracionGuiadaScreenPreview() {
    RespiracionGuiadaScreen(onBackClick = {})
}

@Preview(showBackground = true)
@Composable
private fun PausaConscienteScreenPreview() {
    PausaConscienteScreen(onBackClick = {})
}

@Preview(showBackground = true)
@Composable
private fun MemoriceScreenPreview() {
    MemoriceScreen(onBackClick = {})
}

@Preview(showBackground = true)
@Composable
private fun GatoScreenPreview() {
    GatoScreen(onBackClick = {})
}

@Preview(showBackground = true)
@Composable
private fun CaminoBienestarScreenPreview() {
    CaminoBienestarScreen(onBackClick = {})
}
