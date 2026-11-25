package com.sana_esencia.catalogo_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotBlank(message = "El icono es obligatorio")
    @Column(nullable = false, length = 80)
    private String icono;

    @NotBlank(message = "El profesional es obligatorio")
    @Column(nullable = false, length = 120)
    private String profesional;

    @NotBlank(message = "La modalidad es obligatoria")
    @Column(nullable = false, length = 30)
    private String modalidad; // "Presencial", "Online", "Ambas"

    @Column(name = "agenda_pausada", nullable = false)
    private boolean agendaPausada = false;

    public Especialidad() {}

    public Especialidad(Integer id, String nombre, String descripcion, String icono,
                        String profesional, String modalidad, boolean agendaPausada) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.profesional = profesional;
        this.modalidad = modalidad;
        this.agendaPausada = agendaPausada;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getProfesional() { return profesional; }
    public void setProfesional(String profesional) { this.profesional = profesional; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public boolean isAgendaPausada() { return agendaPausada; }
    public void setAgendaPausada(boolean agendaPausada) { this.agendaPausada = agendaPausada; }
}