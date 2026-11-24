package com.sana_esencia.usuarios_service.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "beneficiarios",
        indexes = {
                @Index(name = "idx_beneficiarios_propietario", columnList = "propietario_email")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_beneficiarios_propietario_rut",
                        columnNames = {"propietario_email", "rut"}
                )
        }
)
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "propietario_email", nullable = false, length = 150)
    private String propietarioEmail;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 50)
    private String parentesco;

    @Column(nullable = false)
    private boolean activo = true;

    public Beneficiario() {
    }

    public Beneficiario(
            String propietarioEmail,
            String nombre,
            String rut,
            LocalDate fechaNacimiento,
            String parentesco
    ) {
        this.propietarioEmail = propietarioEmail;
        this.nombre = nombre;
        this.rut = rut;
        this.fechaNacimiento = fechaNacimiento;
        this.parentesco = parentesco;
        this.activo = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPropietarioEmail() {
        return propietarioEmail;
    }

    public void setPropietarioEmail(String propietarioEmail) {
        this.propietarioEmail = propietarioEmail;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
