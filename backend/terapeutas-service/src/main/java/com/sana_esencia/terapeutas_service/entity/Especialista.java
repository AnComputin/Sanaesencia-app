package com.sana_esencia.terapeutas_service.entity;

import jakarta.persistence.*;

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

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "especialidad_id", nullable = false)
    private Integer especialidadId;

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