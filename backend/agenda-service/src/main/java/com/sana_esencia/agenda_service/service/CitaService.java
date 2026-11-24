package com.sana_esencia.agenda_service.service;

import com.sana_esencia.agenda_service.entity.Cita;
import com.sana_esencia.agenda_service.repository.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CitaService {

    private static final long DURACION_RETENCION_MILLIS = 10 * 60 * 1000L;
    private final CitaRepository repository;

    public CitaService(CitaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Cita> listarPorUsuario(String email) {
        repository.eliminarRetencionesExpiradas(System.currentTimeMillis());
        return repository.findByUsuarioEmailOrderByInicioMillisDesc(email.trim().toLowerCase());
    }

    @Transactional
    public List<Cita> listarPorEspecialista(Integer especialistaId) {
        repository.eliminarRetencionesExpiradas(System.currentTimeMillis());
        return repository.findByEspecialistaIdOrderByInicioMillisAsc(especialistaId);
    }

    @Transactional
    public List<Cita> listarTodas() {
        repository.eliminarRetencionesExpiradas(System.currentTimeMillis());
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Cita obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con id: " + id));
    }

    @Transactional
    public synchronized Cita retener(Cita cita) {
        cita.setEstado("EN_PAGO");
        Cita guardada = guardarNueva(cita);
        guardada.setExpiraEnMillis(System.currentTimeMillis() + DURACION_RETENCION_MILLIS);
        return repository.save(guardada);
    }

    @Transactional
    public synchronized Cita reservar(Cita cita) {
        cita.setEstado("CONFIRMADA");
        cita.setExpiraEnMillis(null);
        return guardarNueva(cita);
    }

    private Cita guardarNueva(Cita cita) {
        if (cita.getInicioMillis() == null || cita.getFinMillis() == null
                || cita.getFinMillis() <= cita.getInicioMillis()) {
            throw new IllegalArgumentException("El rango horario de la cita no es válido");
        }

        repository.eliminarRetencionesExpiradas(System.currentTimeMillis());

        String modalidadNormalizada = cita.getModalidad().trim().toUpperCase();
        if (!modalidadNormalizada.equals("PRESENCIAL") && !modalidadNormalizada.equals("ONLINE")) {
            throw new IllegalArgumentException("La modalidad debe ser PRESENCIAL u ONLINE");
        }

        boolean ocupadoEspecialista = repository.existeSolapamientoEspecialista(
                cita.getEspecialistaId(), cita.getInicioMillis(), cita.getFinMillis());
        if (ocupadoEspecialista) {
            throw new IllegalArgumentException("El horario seleccionado ya no está disponible para este especialista");
        }

        if (modalidadNormalizada.equals("PRESENCIAL") && cita.getBoxAsignado() != null
                && repository.existeSolapamientoBox(cita.getBoxAsignado(), cita.getInicioMillis(), cita.getFinMillis())) {
            throw new IllegalArgumentException("El box seleccionado ya está ocupado en ese horario");
        }

        cita.setId(null);
        cita.setUsuarioEmail(cita.getUsuarioEmail().trim().toLowerCase());
        cita.setEspecialidadNombre(cita.getEspecialidadNombre().trim());
        cita.setEspecialistaNombre(cita.getEspecialistaNombre().trim());
        cita.setPacienteNombre(cita.getPacienteNombre().trim());
        cita.setModalidad(modalidadNormalizada);
        cita.setHora(cita.getHora().trim());
        cita.setBoxAsignado(null);
        cita.setExpiraEnMillis(null);
        return repository.save(cita);
    }

    @Transactional
    public synchronized Cita confirmarRetencion(Integer id, String propietario) {
        Cita cita = obtenerPorId(id);
        validarPropietario(cita, propietario);
        if ("CONFIRMADA".equals(cita.getEstado())) {
            return cita;
        }
        if (!"EN_PAGO".equals(cita.getEstado())
                || cita.getExpiraEnMillis() == null
                || cita.getExpiraEnMillis() <= System.currentTimeMillis()) {
            if ("EN_PAGO".equals(cita.getEstado())) repository.delete(cita);
            throw new IllegalArgumentException("La retención expiró o ya no está disponible");
        }
        cita.setEstado("CONFIRMADA");
        cita.setExpiraEnMillis(null);
        return repository.save(cita);
    }

    @Transactional
    public void liberarRetencion(Integer id, String propietario) {
        Cita cita = obtenerPorId(id);
        validarPropietario(cita, propietario);
        if (!"EN_PAGO".equals(cita.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden liberar retenciones pendientes de pago");
        }
        repository.delete(cita);
    }

    private void validarPropietario(Cita cita, String propietario) {
        if (propietario == null || !cita.getUsuarioEmail().equalsIgnoreCase(propietario)) {
            throw new IllegalArgumentException("La retención no pertenece al usuario autenticado");
        }
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
                    boxAsignado, cita.getInicioMillis(), cita.getFinMillis());
            if (ocupadoBox && !boxAsignado.equals(cita.getBoxAsignado())) {
                throw new IllegalArgumentException("El box " + boxAsignado + " ya está ocupado en ese horario");
            }
            cita.setBoxAsignado(boxAsignado);
        }

        if ("EN_PAGO".equals(cita.getEstado()) && "CONFIRMADA".equals(estadoNormalizado)) {
            throw new IllegalArgumentException("Use el endpoint de confirmación de retención para completar el pago");
        }
        cita.setEstado(estadoNormalizado);
        cita.setExpiraEnMillis(null);
        return repository.save(cita);
    }
}
