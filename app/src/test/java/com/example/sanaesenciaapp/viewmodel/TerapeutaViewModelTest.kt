package com.example.sanaesenciaapp.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TerapeutaViewModelTest {
    @Test
    fun iniciaSinDatosNoVerificadosYRechazaCambiosSobreDatosAusentes() {
        val viewModel = TerapeutaViewModel()

        assertEquals(emptyList<Paciente>(), viewModel.uiState.value.pacientes)
        assertEquals(emptyList<Cita>(), viewModel.uiState.value.citas)

        viewModel.actualizarEstadoCita(10, EstadoCita.ASISTIDA)
        viewModel.seleccionarPaciente(
            Paciente(
                id = 10,
                nombre = "Paciente de prueba",
                rut = "00.000.000-0",
                ultimaAtencion = "",
                boxAsignado = ""
            )
        )

        assertEquals(emptyList<Cita>(), viewModel.uiState.value.citas)
        assertEquals(emptyList<Paciente>(), viewModel.uiState.value.pacientes)
        assertNull(viewModel.uiState.value.pacienteSeleccionado)
    }
}
