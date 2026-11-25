package com.sana_esencia.catalogo_service;

import com.sana_esencia.catalogo_service.controller.EspecialidadController;
import com.sana_esencia.catalogo_service.entity.Especialidad;
import com.sana_esencia.catalogo_service.repository.EspecialidadRepository;
import com.sana_esencia.catalogo_service.security.SecurityConfig;
import com.sana_esencia.catalogo_service.service.EspecialidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CatalogoUnitTests {

    @Mock
    private EspecialidadRepository repository;

    private EspecialidadService service;
    private EspecialidadController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = new EspecialidadService(repository);
        controller = new EspecialidadController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Especialidad muestra(Integer id, String nombre) {
        return new Especialidad(id, nombre, "Desc", "🌿", "5 profesionales", "Ambas", false);
    }

    @Test
    void serviceCrudYEstadoAgenda() {
        Especialidad e = muestra(1, "Psicología");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(e));
        assertEquals(1, service.listarTodas().size());

        when(repository.findById(1)).thenReturn(Optional.of(e));
        assertEquals("Psicología", service.obtenerPorId(1).getNombre());

        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(99));

        Especialidad nueva = muestra(99, "Reiki");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Especialidad creada = service.crear(nueva);
        assertNull(creada.getId());

        Especialidad cambios = muestra(null, "  Psicología Clínica ");
        cambios.setDescripcion("  Nueva desc ");
        cambios.setIcono(" 🧠 ");
        cambios.setProfesional(" 6 ");
        cambios.setModalidad(" Online ");
        cambios.setAgendaPausada(true);

        Especialidad actualizada = service.actualizar(1, cambios);
        assertEquals("Psicología Clínica", actualizada.getNombre());
        assertEquals("Online", actualizada.getModalidad());
        assertTrue(actualizada.isAgendaPausada());

        Especialidad reactivada = service.cambiarEstadoAgenda(1, false);
        assertFalse(reactivada.isAgendaPausada());

        when(repository.existsById(1)).thenReturn(true);
        service.eliminar(1);
        verify(repository).deleteById(1);

        when(repository.existsById(88)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(88));
    }

    @Test
    void controllerEndpointsYNotFound() throws Exception {
        Especialidad e = muestra(1, "Yoga");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(e));
        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Yoga"));

        when(repository.findById(1)).thenReturn(Optional.of(e));
        mockMvc.perform(get("/api/especialidades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        when(repository.findById(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/especialidades/99"))
                .andExpect(status().isNotFound());

        when(repository.save(any())).thenReturn(e);
        mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Yoga","descripcion":"Desc","icono":"🧘","profesional":"3","modalidad":"Ambas","agendaPausada":false}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/especialidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Yoga 2","descripcion":"Desc","icono":"🧘","profesional":"3","modalidad":"Online","agendaPausada":true}
                                """))
                .andExpect(status().isOk());

        ResponseEntity<?> put404 = controller.actualizar(99, e);
        assertEquals(HttpStatus.NOT_FOUND, put404.getStatusCode());

        mockMvc.perform(patch("/api/especialidades/1/agenda-pausada").param("pausada", "true"))
                .andExpect(status().isOk());
        assertEquals(HttpStatus.NOT_FOUND, controller.pausarAgenda(99, true).getStatusCode());

        when(repository.existsById(1)).thenReturn(true);
        mockMvc.perform(delete("/api/especialidades/1"))
                .andExpect(status().isNoContent());

        when(repository.existsById(99)).thenReturn(false);
        mockMvc.perform(delete("/api/especialidades/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void securityConfigJwtDecoderYRoles() {
        SecurityConfig config = new SecurityConfig();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(config, "jwtSecretBase64", secret);

        assertNotNull(config.jwtDecoder());
        var converter = config.jwtAuthenticationConverter();

        Jwt jwtAdmin = Jwt.withTokenValue("t").header("alg", "HS256")
                .subject("admin@sana.cl").claim("rol", "admin")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        var auth = converter.convert(jwtAdmin);
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        Jwt jwtSinRol = Jwt.withTokenValue("t2").header("alg", "HS256")
                .subject("u@sana.cl")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        assertTrue(converter.convert(jwtSinRol).getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
}
