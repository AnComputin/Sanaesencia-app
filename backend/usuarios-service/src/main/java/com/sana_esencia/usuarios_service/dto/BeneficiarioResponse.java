package com.sana_esencia.usuarios_service.dto;

import com.sana_esencia.usuarios_service.entity.Beneficiario;
import java.time.LocalDate;

public class BeneficiarioResponse {

    private Integer id;
    private String propietarioEmail;
    private String nombre;
    private String rut;
    private LocalDate fechaNacimiento;
    private String parentesco;
    private boolean activo;

    public BeneficiarioResponse(Beneficiario beneficiario) {
        this.id = beneficiario.getId();
        this.propietarioEmail = beneficiario.getPropietarioEmail();
        this.nombre = beneficiario.getNombre();
        this.rut = beneficiario.getRut();
        this.fechaNacimiento = beneficiario.getFechaNacimiento();
        this.parentesco = beneficiario.getParentesco();
        this.activo = beneficiario.isActivo();
    }

    public Integer getId() {
        return id;
    }

    public String getPropietarioEmail() {
        return propietarioEmail;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRut() {
        return rut;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getParentesco() {
        return parentesco;
    }

    public boolean isActivo() {
        return activo;
    }
}