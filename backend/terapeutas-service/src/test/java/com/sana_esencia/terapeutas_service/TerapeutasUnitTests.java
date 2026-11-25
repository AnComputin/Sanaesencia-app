package com.sana_esencia.terapeutas_service;

import com.sana_esencia.terapeutas_service.controller.EspecialistaController;
import com.sana_esencia.terapeutas_service.entity.Especialista;
import com.sana_esencia.terapeutas_service.repository.EspecialistaRepository;
import com.sana_esencia.terapeutas_service.security.SecurityConfig;
import com.sana_esencia.terapeutas_service.service.EspecialistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TerapeutasUnitTests {

    @Mock
    private EspecialistaRepository repository;

    private EspecialistaService service;
    private EspecialistaController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = new EspecialistaService(repository);
        controller = new EspecialistaController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Especialista especialista(Integer id) {
        return new Especialista(id, "Dra. Sofía", 1, "Ambas", true);
    }

    @Test
    void serviceListaCreaActualizaYCambiaEstado() {
        Especialista e = especialista(1);
        when(repository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(e));
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(e));
        assertEquals(1, service.listarTodos(true).size());
        assertEquals(1, service.listarTodos(false).size());

        when(repository.findByEspecialidadIdAndActivoTrueOrderByNombreAsc(1)).thenReturn(List.of(e));
        when(repository.findByEspecialidadIdOrderByNombreAsc(1)).thenReturn(List.of(e));
        assertEquals(1, service.listarPorEspecialidad(1, true).size());
        assertEquals(1, service.listarPorEspecialidad(1, false).size());

        when(repository.findById(1)).thenReturn(Optional.of(e));
        assertEquals("Dra. Sofía", service.obtenerPorId(1).getNombre());

        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(99));

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Especialista nuevo = new Especialista(77, "  Dr. Luis ", 2, " Presencial ", false);
        Especialista creado = service.crear(nuevo);
        assertNull(creado.getId());
        assertEquals("Dr. Luis", creado.getNombre());
        assertTrue(creado.isActivo());

        Especialista datos = new Especialista(null, "  Dra. Sofía M. ", 3, " Online ", false);
        Especialista act = service.actualizar(1, datos);
        assertEquals("Dra. Sofía M.", act.getNombre());
        assertEquals(3, act.getEspecialidadId());
        assertFalse(act.isActivo());

        Especialista reactivada = service.cambiarEstadoActivo(1, true);
        assertTrue(reactivada.isActivo());
    }

    @Test
    void controllerEndpointsYErrores404() throws Exception {
        Especialista e = especialista(1);
        when(repository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(e));
        when(repository.findByEspecialidadIdAndActivoTrueOrderByNombreAsc(1)).thenReturn(List.of(e));

        mockMvc.perform(get("/api/especialistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Dra. Sofía"));

        mockMvc.perform(get("/api/especialistas").param("especialidadId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especialidadId").value(1));

        when(repository.findById(1)).thenReturn(Optional.of(e));
        mockMvc.perform(get("/api/especialistas/1")).andExpect(status().isOk());

        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/especialistas/99")).andExpect(status().isNotFound());

        when(repository.save(any())).thenReturn(e);
        mockMvc.perform(post("/api/especialistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Dr. Nuevo","especialidadId":1,"modalidad":"Ambas","activo":true}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/especialistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Dra. Sofía","especialidadId":1,"modalidad":"Online","activo":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/especialistas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"X","especialidadId":1,"modalidad":"Online","activo":true}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/especialistas/1/activo").param("activo", "false"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/especialistas/99/activo").param("activo", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void securityConfigJwtDecoderYRoles() {
        SecurityConfig config = new SecurityConfig();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(config, "jwtSecretBase64", secret);

        assertNotNull(config.jwtDecoder());
        var converter = config.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256")
                .subject("a@sana.cl").claim("rol", "terapeuta")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        assertTrue(converter.convert(jwt).getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TERAPEUTA")));

        Jwt jwtBlank = Jwt.withTokenValue("t2").header("alg", "HS256")
                .subject("u@sana.cl").claim("rol", "  ")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        assertTrue(converter.convert(jwtBlank).getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
}
