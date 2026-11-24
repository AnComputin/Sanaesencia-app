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

    @Transactional(readOnly = true)
    public Cita obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con id: " + id));
    }

    @Transactional
    public Cita reservar(Cita cita) {
        if (cita.getFinMillis() <= cita.getInicioMillis()) {
            throw new IllegalArgumentException("El rango horario de la cita no es válido");
        }

        String modalidadNormalizada = cita.getModalidad().trim().toUpperCase();
        if (!modalidadNormalizada.equals("PRESENCIAL") && !modalidadNormalizada.equals("ONLINE")) {
            throw new IllegalArgumentException("La modalidad debe ser PRESENCIAL u ONLINE");
        }

        boolean ocupadoEspecialista = repository.existeSolapamientoEspecialista(
                cita.getEspecialistaId(),
                cita.getInicioMillis(),
                cita.getFinMillis()
        );
        if (ocupadoEspecialista) {
            throw new IllegalArgumentException("El horario seleccionado ya no está disponible para este especialista");
        }

        if (modalidadNormalizada.equals("PRESENCIAL") && cita.getBoxAsignado() != null) {
            boolean ocupadoBox = repository.existeSolapamientoBox(
                    cita.getBoxAsignado(),
                    cita.getInicioMillis(),
                    cita.getFinMillis()
            );
            if (ocupadoBox) {
                throw new IllegalArgumentException("El box " + cita.getBoxAsignado() + " ya está ocupado en ese horario");
            }
        }

        cita.setId(null);
        cita.setUsuarioEmail(cita.getUsuarioEmail().trim().toLowerCase());
        cita.setEspecialidadNombre(cita.getEspecialidadNombre().trim());
        cita.setEspecialistaNombre(cita.getEspecialistaNombre().trim());
        cita.setPacienteNombre(cita.getPacienteNombre().trim());
        cita.setModalidad(modalidadNormalizada);
        cita.setHora(cita.getHora().trim());

        if (cita.getEstado() == null || cita.getEstado().isBlank()) {
            cita.setEstado("CONFIRMADA");
        } else {
            cita.setEstado(cita.getEstado().trim().toUpperCase());
        }

        return repository.save(cita);
    }

    @Transactional
    public Cita actualizarEstado(Integer id, String nuevoEstado, Integer boxAsignado) {
        Cita cita = obtenerPorId(id);
        String estadoNormalizado = nuevoEstado.trim().toUpperCase();

        if (!estadoNormalizado.equals("CONFIRMADA")
                && !estadoNormalizado.equals("COMPLETADA")
                && !estadoNormalizado.equals("CANCELADA")) {
            throw new IllegalArgumentException("Estado inválido. Valores permitidos: CONFIRMADA, COMPLETADA, CANCELADA");
        }

        if (boxAsignado != null && "PRESENCIAL".equals(cita.getModalidad())) {
            boolean ocupadoBox = repository.existeSolapamientoBox(
                    boxAsignado,
                    cita.getInicioMillis(),
                    cita.getFinMillis()
            );
            if (ocupadoBox && !boxAsignado.equals(cita.getBoxAsignado())) {
                throw new IllegalArgumentException("El box " + boxAsignado + " ya está ocupado en ese horario");
            }
            cita.setBoxAsignado(boxAsignado);
        }

        cita.setEstado(estadoNormalizado);
        return repository.save(cita);
    }
}