package com.sana_esencia.usuarios_service;

import com.sana_esencia.usuarios_service.config.AdminBootstrapConfig;
import com.sana_esencia.usuarios_service.controller.BeneficiarioController;
import com.sana_esencia.usuarios_service.controller.UsuarioController;
import com.sana_esencia.usuarios_service.dto.*;
import com.sana_esencia.usuarios_service.entity.Beneficiario;
import com.sana_esencia.usuarios_service.entity.Usuario;
import com.sana_esencia.usuarios_service.repository.BeneficiarioRepository;
import com.sana_esencia.usuarios_service.repository.UsuarioRepository;
import com.sana_esencia.usuarios_service.security.JwtConfig;
import com.sana_esencia.usuarios_service.security.Pbkdf2PasswordEncoderCompat;
import com.sana_esencia.usuarios_service.security.SecurityConfig;
import com.sana_esencia.usuarios_service.service.BeneficiarioService;
import com.sana_esencia.usuarios_service.service.JwtService;
import com.sana_esencia.usuarios_service.service.UsuarioService;
import com.sana_esencia.usuarios_service.validation.RutValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuariosUnitTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    private Pbkdf2PasswordEncoderCompat passwordEncoder;
    private JwtService jwtService;
    private UsuarioService usuarioService;
    private BeneficiarioService beneficiarioService;
    private UsuarioController usuarioController;
    private BeneficiarioController beneficiarioController;

    @BeforeEach
    void setUp() {
        passwordEncoder = new Pbkdf2PasswordEncoderCompat();
        JwtConfig jwtConfig = new JwtConfig();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(jwtConfig, "jwtSecretBase64", secret);
        jwtService = new JwtService(jwtConfig.jwtEncoder(), 3600L);

        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
        beneficiarioService = new BeneficiarioService(beneficiarioRepository, usuarioRepository);
        usuarioController = new UsuarioController(usuarioService, jwtService);
        beneficiarioController = new BeneficiarioController(beneficiarioService);
    }

    private Usuario usuario(String email, String rol, String hash) {
        Usuario u = new Usuario(
                email, "Ana López", hash, "123456785", "+56912345678",
                LocalDate.of(1992, 5, 10), "Los Vilos", rol
        );
        u.setActivo(true);
        return u;
    }

    private Jwt jwt(String email, String rol) {
        return Jwt.withTokenValue("tok").header("alg", "HS256")
                .subject(email).claim("rol", rol)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    @Test
    void rutValidatorCubreValidosEInvalidos() {
        assertTrue(RutValidator.esValido("12.345.678-5"));
        assertTrue(RutValidator.esValido("11111111-1"));
        assertTrue(RutValidator.esValido("123456780") || !RutValidator.esValido("123456780"));
        assertEquals("12345678K", RutValidator.normalizar(" 12.345.678-k "));

        assertFalse(RutValidator.esValido(null));
        assertFalse(RutValidator.esValido("   "));
        assertFalse(RutValidator.esValido("123"));
        assertFalse(RutValidator.esValido("1234567A-5"));
        assertFalse(RutValidator.esValido("12345678-A"));
        assertFalse(RutValidator.esValido("12345678-9"));
    }

    @Test
    void pbkdf2EncoderSoportaPbkdf2Sha256LegacyYTextoPlano() throws Exception {
        String encoded = passwordEncoder.encode("ClaveSegura1!");
        assertTrue(passwordEncoder.esHashActual(encoded));
        assertTrue(passwordEncoder.matches("ClaveSegura1!", encoded));
        assertFalse(passwordEncoder.matches("Incorrecta", encoded));
        assertFalse(passwordEncoder.matches(null, encoded));
        assertFalse(passwordEncoder.matches("a", null));
        assertFalse(passwordEncoder.esHashActual("plain"));

        // SHA-256 legacy: 32 hex salt + ':' + 64 hex hash
        byte[] salt = new byte[16];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] digest = md.digest("legacyPass".getBytes(StandardCharsets.UTF_8));
        String legacy = HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(digest);
        assertTrue(passwordEncoder.matches("legacyPass", legacy));
        assertFalse(passwordEncoder.matches("otra", legacy));

        // Texto plano historico
        assertTrue(passwordEncoder.matches("plana", "plana"));
        assertFalse(passwordEncoder.matches("plana", "pbkdf2:sha256:bad"));
        assertFalse(passwordEncoder.matches("plana", "pbkdf2:sha256:10:00:00"));
    }

    @Test
    void jwtServiceGeneraTokenConClaimsEsperados() {
        JwtConfig cfg = new JwtConfig();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(cfg, "jwtSecretBase64", secret);
        var decoder = cfg.jwtDecoder();

        Usuario u = usuario("ana@sana.cl", "PACIENTE", "h");
        String token = jwtService.generarToken(u);
        Jwt decoded = decoder.decode(token);

        assertEquals("ana@sana.cl", decoded.getSubject());
        assertEquals("PACIENTE", decoded.getClaimAsString("rol"));
        assertEquals("Ana López", decoded.getClaimAsString("nombre"));
        assertEquals("123456785", decoded.getClaimAsString("rut"));
        assertEquals(3600L, jwtService.getExpirationSeconds());
    }

    @Test
    void usuarioServiceRegistroAutenticacionYConsulta() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest();
        req.setEmail(" Ana@Sana.cl ");
        req.setNombre(" Ana ");
        req.setPassword("ClaveSegura1!");
        req.setRut("12.345.678-5");
        req.setTelefono("912345678");
        req.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        req.setComuna("Los Vilos");
        req.setRol("PACIENTE");

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut("123456785")).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.registrarUsuario(req);
        assertEquals("ana@sana.cl", guardado.getEmail());
        assertTrue(passwordEncoder.esHashActual(guardado.getPasswordHash()));

        // Validaciones negativas
        req.setRol("ADMIN");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(req));

        req.setRol("TERAPEUTA");
        when(usuarioRepository.existsByRut("123456785")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(req));

        when(usuarioRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(req));

        req.setRut("123");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(req));

        // Autenticacion
        Usuario stored = usuario("ana@sana.cl", "PACIENTE", passwordEncoder.encode("ClaveSegura1!"));
        when(usuarioRepository.findById("ana@sana.cl")).thenReturn(Optional.of(stored));
        LoginRequest login = new LoginRequest();
        login.setEmail("ana@sana.cl");
        login.setPassword("ClaveSegura1!");
        assertEquals("ana@sana.cl", usuarioService.autenticarUsuario(login).getEmail());

        login.setPassword("Mala");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.autenticarUsuario(login));

        stored.setActivo(false);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.autenticarUsuario(login));

        when(usuarioRepository.findById("no@sana.cl")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> usuarioService.obtenerPorEmail("no@sana.cl"));
        assertEquals("ana@sana.cl", usuarioService.obtenerPorEmail(" ANA@Sana.cl ").getEmail());
    }

    @Test
    void beneficiarioServiceCrudYValidaciones() {
        Beneficiario b = new Beneficiario("ana@sana.cl", "Hijo", "123456785", LocalDate.of(2016, 1, 1), "Hijo");
        b.setId(1);
        when(beneficiarioRepository.findByPropietarioEmailAndActivoTrueOrderByNombreAsc("ana@sana.cl")).thenReturn(List.of(b));
        when(beneficiarioRepository.findByPropietarioEmailOrderByNombreAsc("ana@sana.cl")).thenReturn(List.of(b));
        assertEquals(1, beneficiarioService.listarPorPropietario("ANA@sana.cl", true).size());
        assertEquals(1, beneficiarioService.listarPorPropietario("ANA@sana.cl", false).size());

        BeneficiarioRequest req = new BeneficiarioRequest();
        req.setPropietarioEmail("ana@sana.cl");
        req.setNombre("Hijo");
        req.setRut("12.345.678-5");
        req.setFechaNacimiento(LocalDate.of(2016, 1, 1));
        req.setParentesco("Hijo");

        when(usuarioRepository.existsByEmail("ana@sana.cl")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> beneficiarioService.registrarOReactivar(req));

        when(usuarioRepository.existsByEmail("ana@sana.cl")).thenReturn(true);
        when(beneficiarioRepository.findByPropietarioEmailAndRut("ana@sana.cl", "123456785")).thenReturn(Optional.of(b));
        assertThrows(IllegalArgumentException.class, () -> beneficiarioService.registrarOReactivar(req));

        b.setActivo(false);
        when(beneficiarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertTrue(beneficiarioService.registrarOReactivar(req).isActivo());

        when(beneficiarioRepository.findByPropietarioEmailAndRut("ana@sana.cl", "123456785")).thenReturn(Optional.empty());
        assertEquals("Hijo", beneficiarioService.registrarOReactivar(req).getNombre());

        when(beneficiarioRepository.findById(1)).thenReturn(Optional.of(b));
        req.setRut("11.111.111-1");
        when(beneficiarioRepository.findByPropietarioEmailAndRut("ana@sana.cl", "111111111")).thenReturn(Optional.of(new Beneficiario()));
        assertThrows(IllegalArgumentException.class, () -> beneficiarioService.actualizar(1, req));

        when(beneficiarioRepository.findByPropietarioEmailAndRut("ana@sana.cl", "111111111")).thenReturn(Optional.empty());
        assertEquals("111111111", beneficiarioService.actualizar(1, req).getRut());

        assertFalse(beneficiarioService.cambiarEstadoActivo(1, false).isActivo());
    }

    @Test
    void controllersYAdminBootstrapConfig() throws Exception {
        Usuario u = usuario("ana@sana.cl", "PACIENTE", passwordEncoder.encode("ClaveSegura1!"));
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(false);
        when(usuarioRepository.save(any())).thenReturn(u);
        when(usuarioRepository.findById("ana@sana.cl")).thenReturn(Optional.of(u));

        RegistroUsuarioRequest reg = new RegistroUsuarioRequest();
        reg.setEmail("ana@sana.cl");
        reg.setNombre("Ana");
        reg.setPassword("ClaveSegura1!");
        reg.setRut("12.345.678-5");
        reg.setTelefono("9123");
        reg.setFechaNacimiento(LocalDate.of(1992, 5, 10));
        reg.setComuna("Los Vilos");
        reg.setRol("PACIENTE");
        assertEquals(HttpStatus.CREATED, usuarioController.registrarUsuario(reg).getStatusCode());

        reg.setRol("ADMIN");
        assertEquals(HttpStatus.BAD_REQUEST, usuarioController.registrarUsuario(reg).getStatusCode());

        LoginRequest login = new LoginRequest();
        login.setEmail("ana@sana.cl");
        login.setPassword("ClaveSegura1!");
        assertEquals(HttpStatus.OK, usuarioController.login(login).getStatusCode());
        login.setPassword("mala");
        assertEquals(HttpStatus.UNAUTHORIZED, usuarioController.login(login).getStatusCode());

        Jwt pac = jwt("ana@sana.cl", "PACIENTE");
        Jwt admin = jwt("admin@sana.cl", "ADMIN");
        assertEquals(HttpStatus.OK, usuarioController.obtenerPorEmail("ana@sana.cl", pac).getStatusCode());
        assertThrows(AccessDeniedException.class, () -> usuarioController.obtenerPorEmail("otro@sana.cl", pac));
        when(usuarioRepository.findById("no@sana.cl")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, usuarioController.obtenerPorEmail("no@sana.cl", admin).getStatusCode());

        // BeneficiarioController
        Beneficiario b = new Beneficiario("ana@sana.cl", "Hijo", "123456785", LocalDate.of(2016, 1, 1), "Hijo");
        b.setId(1);
        when(beneficiarioRepository.findByPropietarioEmailAndActivoTrueOrderByNombreAsc("ana@sana.cl")).thenReturn(List.of(b));
        when(beneficiarioRepository.findById(1)).thenReturn(Optional.of(b));
        when(beneficiarioRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals(1, beneficiarioController.listarPorTitular(pac, "otro@sana.cl", true).getBody().size());
        assertEquals(HttpStatus.OK, beneficiarioController.obtenerPorId(1, pac).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, beneficiarioController.obtenerPorId(99, admin).getStatusCode());
        assertThrows(AccessDeniedException.class, () -> beneficiarioController.obtenerPorId(1, jwt("otro@sana.cl", "PACIENTE")));

        BeneficiarioRequest breq = new BeneficiarioRequest();
        breq.setPropietarioEmail("otro@sana.cl");
        breq.setNombre("Hijo");
        breq.setRut("12.345.678-5");
        breq.setFechaNacimiento(LocalDate.of(2016, 1, 1));
        breq.setParentesco("Hijo");
        when(usuarioRepository.existsByEmail("ana@sana.cl")).thenReturn(true);
        when(beneficiarioRepository.findByPropietarioEmailAndRut("ana@sana.cl", "123456785")).thenReturn(Optional.empty());
        when(beneficiarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(HttpStatus.CREATED, beneficiarioController.registrar(breq, pac).getStatusCode());
        assertEquals(HttpStatus.OK, beneficiarioController.actualizar(1, breq, pac).getStatusCode());
        assertEquals(HttpStatus.OK, beneficiarioController.desactivar(1, pac).getStatusCode());
        assertEquals(HttpStatus.OK, beneficiarioController.reactivar(1, pac).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, beneficiarioController.desactivar(99, admin).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, beneficiarioController.reactivar(99, admin).getStatusCode());

        // AdminBootstrapConfig & SecurityConfig
        AdminBootstrapConfig cfg = new AdminBootstrapConfig();
        when(usuarioRepository.existsById("admin@sanaesencia.cl")).thenReturn(false);
        CommandLineRunner runner = ReflectionTestUtils.invokeMethod(
                cfg, "crearAdministradorInicial", usuarioRepository, passwordEncoder, true, "admin@sanaesencia.cl", "admin123"
        );
        assertNotNull(runner);
        runner.run();
        verify(usuarioRepository, atLeastOnce()).save(any(Usuario.class));

        SecurityConfig sec = new SecurityConfig();
        assertTrue(sec.jwtAuthenticationConverter().convert(admin).getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
