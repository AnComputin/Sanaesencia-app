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

        if (modalidadNormalizada.equals("PRESENCIAL")) {
            List<Integer> ocupados = repository.findBoxesOcupados(
                    cita.getInicioMillis(),
                    cita.getFinMillis()
            );
            Integer boxSolicitado = cita.getBoxAsignado();
            if (boxSolicitado != null) {
                if (boxSolicitado < 1 || boxSolicitado > 8) {
                    throw new IllegalArgumentException("El box asignado debe estar entre 1 y 8");
                }
                if (ocupados.contains(boxSolicitado)) {
                    throw new IllegalArgumentException("El box " + boxSolicitado + " ya está ocupado en ese horario");
                }
            } else {
                Integer boxLibre = null;
                for (int b = 1; b <= 8; b++) {
                    if (!ocupados.contains(b)) {
                        boxLibre = b;
                        break;
                    }
                }
                if (boxLibre == null) {
                    throw new IllegalArgumentException("No hay boxes disponibles en ese horario (8/8 ocupados)");
                }
                cita.setBoxAsignado(boxLibre);
            }
        } else {
            cita.setBoxAsignado(null);
        }

        cita.setId(null);
        cita.setUsuarioEmail(cita.getUsuarioEmail().trim().toLowerCase());
        cita.setEspecialidadNombre(cita.getEspecialidadNombre().trim());
        cita.setEspecialistaNombre(cita.getEspecialistaNombre().trim());
        cita.setPacienteNombre(cita.getPacienteNombre().trim());
        cita.setModalidad(modalidadNormalizada);
        cita.setHora(cita.getHora().trim());

        // El estado inicial lo determina el servidor; nunca se acepta desde el cliente.
        cita.setEstado("CONFIRMADA");

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