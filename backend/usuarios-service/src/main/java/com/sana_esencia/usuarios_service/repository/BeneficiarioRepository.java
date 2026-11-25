package com.sana_esencia.usuarios_service.repository;

import com.sana_esencia.usuarios_service.entity.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Integer> {

    List<Beneficiario> findByPropietarioEmailAndActivoTrueOrderByNombreAsc(String propietarioEmail);

    List<Beneficiario> findByPropietarioEmailOrderByNombreAsc(String propietarioEmail);

    Optional<Beneficiario> findByPropietarioEmailAndRut(String propietarioEmail, String rut);
}