package com.sana_esencia.catalogo_service.service;

import com.sana_esencia.catalogo_service.entity.Especialidad;
import com.sana_esencia.catalogo_service.repository.EspecialidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecialidadService {

    private final EspecialidadRepository repository;

    public EspecialidadService(EspecialidadRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> listarTodas() {
        return repository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Especialidad obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada con id: " + id));
    }

    @Transactional
    public Especialidad crear(Especialidad especialidad) {
        especialidad.setId(null);
        return repository.save(especialidad);
    }

    @Transactional
    public Especialidad actualizar(Integer id, Especialidad datos) {
        Especialidad actual = obtenerPorId(id);
        actual.setNombre(datos.getNombre().trim());
        actual.setDescripcion(datos.getDescripcion().trim());
        actual.setIcono(datos.getIcono().trim());
        actual.setProfesional(datos.getProfesional().trim());
        actual.setModalidad(datos.getModalidad().trim());
        actual.setAgendaPausada(datos.isAgendaPausada());
        return repository.save(actual);
    }

    @Transactional
    public Especialidad cambiarEstadoAgenda(Integer id, boolean pausada) {
        Especialidad actual = obtenerPorId(id);
        actual.setAgendaPausada(pausada);
        return repository.save(actual);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Especialidad no encontrada con id: " + id);
        }
        repository.deleteById(id);
    }
}
