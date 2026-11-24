package com.sana_esencia.agenda_service.service;

import com.sana_esencia.agenda_service.entity.Cita;
import com.sana_esencia.agenda_service.repository.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CitaService {

    private final CitaRepository repository;

    public CitaService(CitaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Cita> listarPorUsuario(String email) {
        return repository.findByUsuarioEmailOrderByInicioMillisDesc(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<Cita> listarPorEspecialista(Integer especialistaId) {
        return repository.findByEspecialistaIdOrderByInicioMillisAsc(especialistaId);
    }

    @Transactional(readOnly = true)
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    @Transactional
    public Cita reservar(Cita cita) {
        if (cita.getInicioMillis() == null || cita.getFinMillis() == null
                || cita.getFinMillis() <= cita.getInicioMillis()) {
            throw new IllegalArgumentException("El rango horario de la cita no es válido");
        }

        boolean ocupado = repository.existeSolapamientoEspecialista(
                cita.getEspecialistaId(),
                cita.getInicioMillis(),
                cita.getFinMillis()
        );

        if (ocupado) {
            throw new IllegalArgumentException("El horario seleccionado ya no está disponible para este especialista");
        }

        cita.setId(null);
        cita.setUsuarioEmail(cita.getUsuarioEmail().trim().toLowerCase());
        cita.setModalidad(cita.getModalidad().trim().toUpperCase());
        if (cita.getEstado() == null || cita.getEstado().isBlank()) {
            cita.setEstado("CONFIRMADA");
        }

        return repository.save(cita);
    }

    @Transactional
    public Cita actualizarEstado(Integer id, String nuevoEstado, Integer boxAsignado) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con id: " + id));
        cita.setEstado(nuevoEstado.trim().toUpperCase());
        if (boxAsignado != null) {
            cita.setBoxAsignado(boxAsignado);
        }
        return repository.save(cita);
    }
}
