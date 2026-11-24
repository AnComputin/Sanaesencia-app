package com.sana_esencia.catalogo_service.repository;

import com.sana_esencia.catalogo_service.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    List<Especialidad> findAllByOrderByNombreAsc();
}