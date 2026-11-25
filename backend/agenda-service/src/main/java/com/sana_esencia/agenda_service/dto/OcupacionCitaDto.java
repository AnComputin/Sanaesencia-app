package com.sana_esencia.agenda_service.dto;

public record OcupacionCitaDto(
        Integer especialistaId,
        String modalidad,
        Long inicioMillis,
        Long finMillis
) {
}
