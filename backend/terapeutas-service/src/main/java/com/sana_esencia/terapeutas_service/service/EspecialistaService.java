package com.sana_esencia.terapeutas_service.service;

import com.sana_esencia.terapeutas_service.entity.Especialista;
import com.sana_esencia.terapeutas_service.repository.EspecialistaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecialistaService {

    private final EspecialistaRepository repository;

    public EspecialistaService(EspecialistaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Especialista> listarTodos(boolean soloActivos) {
        return soloActivos
                ? repository.findByActivoTrueOrderByNombreAsc()
                : repository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Especialista> listarPorEspecialidad(Integer especialidadId, boolean soloActivos) {
        return soloActivos
                ? repository.findByEspecialidadIdAndActivoTrueOrderByNombreAsc(especialidadId)
                : repository.findByEspecialidadIdOrderByNombreAsc(especialidadId);
    }

    @Transactional(readOnly = true)
    public Especialista obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialista no encontrado con id: " + id));
    }

    @Transactional
    public Especialista crear(Especialista especialista) {
        especialista.setId(null);
        especialista.setNombre(especialista.getNombre().trim());
        especialista.setModalidad(especialista.getModalidad().trim());
        especialista.setActivo(true);
        return repository.save(especialista);
    }

    @Transactional
    public Especialista actualizar(Integer id, Especialista datos) {
        Especialista actual = obtenerPorId(id);
        actual.setNombre(datos.getNombre().trim());
        actual.setEspecialidadId(datos.getEspecialidadId());
        actual.setModalidad(datos.getModalidad().trim());
        actual.setActivo(datos.isActivo());
        return repository.save(actual);
    }

    @Transactional
    public Especialista cambiarEstadoActivo(Integer id, boolean activo) {
        Especialista actual = obtenerPorId(id);
        actual.setActivo(activo);
        return repository.save(actual);
    }
}