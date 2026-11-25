package com.sana_esencia.usuarios_service.dto;

public class AuthResponse {

    private String token;
    private String tipo = "Bearer";
    private long expiraEnSegundos;
    private UsuarioResponse usuario;

    public AuthResponse(String token, long expiraEnSegundos, UsuarioResponse usuario) {
        this.token = token;
        this.expiraEnSegundos = expiraEnSegundos;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }
}