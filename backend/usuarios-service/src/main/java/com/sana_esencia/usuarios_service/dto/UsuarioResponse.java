
package com.sana_esencia.usuarios_service.dto;

import com.sana_esencia.usuarios_service.entity.Usuario;
import java.time.LocalDate;

public class UsuarioResponse {

    private String email;
    private String nombre;
    private String rut;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String comuna;
    private String rol;
    private boolean activo;

    public UsuarioResponse(Usuario usuario) {
        this.email = usuario.getEmail();
        this.nombre = usuario.getNombre();
        this.rut = usuario.getRut();
        this.telefono = usuario.getTelefono();
        this.fechaNacimiento = usuario.getFechaNacimiento();
        this.comuna = usuario.getComuna();
        this.rol = usuario.getRol();
        this.activo = usuario.isActivo();
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRut() {
        return rut;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getComuna() {
        return comuna;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
