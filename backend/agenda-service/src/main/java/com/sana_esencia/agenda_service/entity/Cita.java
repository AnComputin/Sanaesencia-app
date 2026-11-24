package com.sana_esencia.agenda_service.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "citas",
        indexes = {
                @Index(name = "idx_citas_usuario", columnList = "usuario_email"),
                @Index(name = "idx_citas_especialista_horario", columnList = "especialista_id, inicio_millis, fin_millis")
        }
)
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_email", nullable = false, length = 150)
    private String usuarioEmail;

    @Column(name = "especialidad_id", nullable = false)
    private Integer especialidadId;

    @Column(name = "especialidad_nombre", nullable = false, length = 120)
    private String especialidadNombre;

    @Column(name = "especialista_id", nullable = false)
    private Integer especialistaId;

    @Column(name = "especialista_nombre", nullable = false, length = 120)
    private String especialistaNombre;

    @Column(nullable = false, length = 30)
    private String modalidad; // "PRESENCIAL", "ONLINE"

    @Column(name = "inicio_millis", nullable = false)
    private Long inicioMillis;

    @Column(name = "fin_millis", nullable = false)
    private Long finMillis;

    @Column(nullable = false, length = 20)
    private String hora;

    @Column(name = "paciente_nombre", nullable = false, length = 120)
    private String pacienteNombre;

    @Column(name = "beneficiario_id")
    private Integer beneficiarioId;

    @Column(nullable = false, length = 30)
    private String estado; // "CONFIRMADA", "COMPLETADA", "CANCELADA"

    @Column(name = "box_asignado")
    private Integer boxAsignado;

    public Cita() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    public Integer getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Integer especialidadId) { this.especialidadId = especialidadId; }
    public String getEspecialidadNombre() { return especialidadNombre; }
    public void setEspecialidadNombre(String especialidadNombre) { this.especialidadNombre = especialidadNombre; }
    public Integer getEspecialistaId() { return especialistaId; }
    public void setEspecialistaId(Integer especialistaId) { this.especialistaId = especialistaId; }
    public String getEspecialistaNombre() { return especialistaNombre; }
    public void setEspecialistaNombre(String especialistaNombre) { this.especialistaNombre = especialistaNombre; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public Long getInicioMillis() { return inicioMillis; }
    public void setInicioMillis(Long inicioMillis) { this.inicioMillis = inicioMillis; }
    public Long getFinMillis() { return finMillis; }
    public void setFinMillis(Long finMillis) { this.finMillis = finMillis; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public Integer getBeneficiarioId() { return beneficiarioId; }
    public void setBeneficiarioId(Integer beneficiarioId) { this.beneficiarioId = beneficiarioId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getBoxAsignado() { return boxAsignado; }
    public void setBoxAsignado(Integer boxAsignado) { this.boxAsignado = boxAsignado; }
}