package com.sana_esencia.agenda_service.repository;

import com.sana_esencia.agenda_service.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findByUsuarioEmailOrderByInicioMillisDesc(String usuarioEmail);

    List<Cita> findByEspecialistaIdOrderByInicioMillisAsc(Integer especialistaId);

    @Query("""
        SELECT COUNT(c) > 0 FROM Cita c
        WHERE c.especialistaId = :especialistaId
          AND c.estado <> 'CANCELADA'
          AND c.inicioMillis < :finMillis
          AND c.finMillis > :inicioMillis
    """)
    boolean existeSolapamientoEspecialista(
            @Param("especialistaId") Integer especialistaId,
            @Param("inicioMillis") Long inicioMillis,
            @Param("finMillis") Long finMillis
    );

    @Query("""
        SELECT COUNT(c) > 0 FROM Cita c
        WHERE c.boxAsignado = :boxAsignado
          AND c.modalidad = 'PRESENCIAL'
          AND c.estado <> 'CANCELADA'
          AND c.inicioMillis < :finMillis
          AND c.finMillis > :inicioMillis
    """)
    boolean existeSolapamientoBox(
            @Param("boxAsignado") Integer boxAsignado,
            @Param("inicioMillis") Long inicioMillis,
            @Param("finMillis") Long finMillis
    );
}