package com.sana_esencia.agenda_service;

import com.sana_esencia.agenda_service.controller.CitaController;
import com.sana_esencia.agenda_service.dto.OcupacionCitaDto;
import com.sana_esencia.agenda_service.entity.Cita;
import com.sana_esencia.agenda_service.repository.CitaRepository;
import com.sana_esencia.agenda_service.security.SecurityConfig;
import com.sana_esencia.agenda_service.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaUnitTests {

    @Mock
    private CitaRepository repository;

    private CitaService service;
    private CitaController controller;

    @BeforeEach
    void setUp() {
        service = new CitaService(repository);
        controller = new CitaController(service);
    }

    private Cita citaBase(String modalidad) {
        Cita c = new Cita();
        c.setId(1);
        c.setUsuarioEmail("PAC@Sana.cl");
        c.setEspecialidadId(1);
        c.setEspecialidadNombre("Psicología");
        c.setEspecialistaId(2);
        c.setEspecialistaNombre("Dra. Sofía");
        c.setModalidad(modalidad);
        c.setInicioMillis(1000L);
        c.setFinMillis(3601000L);
        c.setHora("09:00");
        c.setPacienteNombre("Ana");
        c.setBeneficiarioId(5);
        c.setEstado("CONFIRMADA");
        return c;
    }

    private Jwt jwt(String email, String rol) {
        return Jwt.withTokenValue("tok").header("alg", "HS256")
                .subject(email).claim("rol", rol)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    @Test
    void serviceReservarPresencialOnlineYValidaciones() {
        // Rango invalido
        Cita invalida = citaBase("PRESENCIAL");
        invalida.setFinMillis(1000L);
        assertThrows(IllegalArgumentException.class, () -> service.reservar(invalida));

        // Modalidad invalida
        assertThrows(IllegalArgumentException.class, () -> service.reservar(citaBase("HIBRIDA")));

        // Especialista ocupado
        when(repository.existeSolapamientoEspecialista(2, 1000L, 3601000L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.reservar(citaBase("PRESENCIAL")));

        // Presencial: box solicitado fuera de rango o ocupado
        when(repository.existeSolapamientoEspecialista(2, 1000L, 3601000L)).thenReturn(false);
        when(repository.findBoxesOcupados(1000L, 3601000L)).thenReturn(List.of(1, 2));
        Cita boxFuera = citaBase("PRESENCIAL");
        boxFuera.setBoxAsignado(9);
        assertThrows(IllegalArgumentException.class, () -> service.reservar(boxFuera));

        Cita boxOcupado = citaBase("PRESENCIAL");
        boxOcupado.setBoxAsignado(1);
        assertThrows(IllegalArgumentException.class, () -> service.reservar(boxOcupado));

        // Presencial: 8/8 boxes ocupados
        when(repository.findBoxesOcupados(1000L, 3601000L)).thenReturn(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        assertThrows(IllegalArgumentException.class, () -> service.reservar(citaBase("PRESENCIAL")));

        // Presencial autoasigna box 2 cuando box 1 esta ocupado
        when(repository.findBoxesOcupados(1000L, 3601000L)).thenReturn(List.of(1));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Cita guardadaPresencial = service.reservar(citaBase("presencial"));
        assertNull(guardadaPresencial.getId());
        assertEquals(2, guardadaPresencial.getBoxAsignado());
        assertEquals("pac@sana.cl", guardadaPresencial.getUsuarioEmail());
        assertEquals("CONFIRMADA", guardadaPresencial.getEstado());
        assertEquals(5, guardadaPresencial.getBeneficiarioId());

        // Online limpia boxAsignado
        Cita online = citaBase("online");
        online.setBoxAsignado(4);
        Cita guardadaOnline = service.reservar(online);
        assertNull(guardadaOnline.getBoxAsignado());
    }

    @Test
    void serviceConsultasYActualizarEstado() {
        Cita c = citaBase("PRESENCIAL");
        c.setBoxAsignado(1);
        when(repository.findByUsuarioEmailOrderByInicioMillisDesc("pac@sana.cl")).thenReturn(List.of(c));
        when(repository.findByEspecialistaIdOrderByInicioMillisAsc(2)).thenReturn(List.of(c));
        when(repository.findAll()).thenReturn(List.of(c));
        when(repository.findById(1)).thenReturn(Optional.of(c));
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertEquals(1, service.listarPorUsuario(" PAC@sana.cl ").size());
        assertEquals(1, service.listarPorEspecialista(2).size());
        assertEquals(1, service.listarTodas().size());
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPorId(99));

        assertThrows(IllegalArgumentException.class, () -> service.actualizarEstado(1, "OTRO", null));

        when(repository.existeSolapamientoBox(3, 1000L, 3601000L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.actualizarEstado(1, "COMPLETADA", 3));

        when(repository.existeSolapamientoBox(4, 1000L, 3601000L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Cita act = service.actualizarEstado(1, "completada", 4);
        assertEquals("COMPLETADA", act.getEstado());
        assertEquals(4, act.getBoxAsignado());
    }

    @Test
    void controllerAutorizacionPorRolYPropietario() {
        Cita c = citaBase("PRESENCIAL");
        c.setUsuarioEmail("pac@sana.cl");
        Jwt paciente = jwt("pac@sana.cl", "PACIENTE");
        Jwt otroPaciente = jwt("otro@sana.cl", "PACIENTE");
        Jwt admin = jwt("admin@sana.cl", "ADMIN");

        when(repository.findByUsuarioEmailOrderByInicioMillisDesc("pac@sana.cl")).thenReturn(List.of(c));
        assertEquals(1, controller.listar(paciente, "otro@sana.cl", null).getBody().size());
        assertThrows(AccessDeniedException.class, () -> controller.listar(paciente, null, 2));

        when(repository.findAll()).thenReturn(List.of(c));
        when(repository.findByEspecialistaIdOrderByInicioMillisAsc(2)).thenReturn(List.of(c));
        assertEquals(1, controller.listar(admin, "pac@sana.cl", null).getBody().size());
        assertEquals(1, controller.listar(admin, null, 2).getBody().size());
        assertEquals(1, controller.listar(admin, null, null).getBody().size());

        when(repository.findOcupacionActiva()).thenReturn(List.of(new OcupacionCitaDto(2, "PRESENCIAL", 1000L, 3601000L)));
        assertEquals(1, controller.listarOcupacion().getBody().size());
        assertEquals(2, controller.listarOcupacion().getBody().get(0).especialistaId());

        when(repository.findById(1)).thenReturn(Optional.of(c));
        assertEquals(HttpStatus.OK, controller.obtenerPorId(1, paciente).getStatusCode());
        assertThrows(AccessDeniedException.class, () -> controller.obtenerPorId(1, otroPaciente));

        when(repository.findById(99)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(99, admin).getStatusCode());

        // Reservar
        when(repository.existeSolapamientoEspecialista(2, 1000L, 3601000L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ResponseEntity<?> creada = controller.reservar(citaBase("ONLINE"), paciente);
        assertEquals(HttpStatus.CREATED, creada.getStatusCode());

        // Cambiar estado
        assertThrows(AccessDeniedException.class, () -> controller.cambiarEstado(1, "CANCELADA", null, otroPaciente));
        assertThrows(AccessDeniedException.class, () -> controller.cambiarEstado(1, "COMPLETADA", null, paciente));
        assertEquals(HttpStatus.OK, controller.cambiarEstado(1, "CANCELADA", null, paciente).getStatusCode());
        assertEquals(HttpStatus.OK, controller.cambiarEstado(1, "COMPLETADA", null, admin).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.cambiarEstado(1, "INVALIDO", null, admin).getStatusCode());
    }

    @Test
    void securityConfigJwtDecoderYRoles() {
        SecurityConfig config = new SecurityConfig();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(config, "jwtSecretBase64", secret);
        assertNotNull(config.jwtDecoder());
        var converter = config.jwtAuthenticationConverter();
        assertTrue(converter.convert(jwt("a@b.cl", "paciente")).getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE")));
        assertTrue(converter.convert(jwt("a@b.cl", null)).getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
}
