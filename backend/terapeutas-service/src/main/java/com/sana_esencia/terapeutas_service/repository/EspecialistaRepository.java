package com.sana_esencia.terapeutas_service.repository;

import com.sana_esencia.terapeutas_service.entity.Especialista;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EspecialistaRepository extends JpaRepository<Especialista, Integer> {
    List<Especialista> findByEspecialidadIdAndActivoTrue(Integer especialidadId);
    List<Especialista> findByActivoTrue();
}
