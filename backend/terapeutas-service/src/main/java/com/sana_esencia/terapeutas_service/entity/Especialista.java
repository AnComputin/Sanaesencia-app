package com.sana_esencia.terapeutas_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "especialistas",
        indexes = {
                @Index(name = "idx_especialistas_especialidad", columnList = "especialidad_id")
        }
)
public class Especialista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre del especialista es obligatorio")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotNull(message = "El ID de especialidad es obligatorio")
    @Column(name = "especialidad_id", nullable = false)
    private Integer especialidadId;

    @NotBlank(message = "La modalidad es obligatoria")
    @Column(nullable = false, length = 30)
    private String modalidad; // "Presencial", "Online", "Ambas"

    @Column(nullable = false)
    private boolean activo = true;

    public Especialista() {}

    public Especialista(Integer id, String nombre, Integer especialidadId, String modalidad, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.especialidadId = especialidadId;
        this.modalidad = modalidad;
        this.activo = activo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Integer especialidadId) { this.especialidadId = especialidadId; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}