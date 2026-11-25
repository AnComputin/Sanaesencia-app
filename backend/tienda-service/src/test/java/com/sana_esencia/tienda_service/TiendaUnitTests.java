package com.sana_esencia.tienda_service;

import com.sana_esencia.tienda_service.controller.TiendaController;
import com.sana_esencia.tienda_service.entity.DetalleVenta;
import com.sana_esencia.tienda_service.entity.Producto;
import com.sana_esencia.tienda_service.entity.Venta;
import com.sana_esencia.tienda_service.repository.ProductoRepository;
import com.sana_esencia.tienda_service.repository.VentaRepository;
import com.sana_esencia.tienda_service.security.SecurityConfig;
import com.sana_esencia.tienda_service.service.TiendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
class TiendaUnitTests {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private VentaRepository ventaRepository;

    private TiendaService service;
    private TiendaController controller;

    @BeforeEach
    void setUp() {
        service = new TiendaService(productoRepository, ventaRepository);
        controller = new TiendaController(service);
    }

    private Producto producto(Integer id, int precio, int stock, boolean activo) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Aceite Lavanda");
        p.setDescripcion("Esencial");
        p.setCategoria("Aromaterapia");
        p.setPrecio(precio);
        p.setStock(stock);
        p.setIcono("🌿");
        p.setActivo(activo);
        return p;
    }

    private Jwt jwt(String email, String rol) {
        return Jwt.withTokenValue("tok").header("alg", "HS256")
                .subject(email).claim("rol", rol)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    @Test
    void serviceCrudProductos() {
        Producto p = producto(1, 5000, 10, true);
        when(productoRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(p));
        when(productoRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(p));
        assertEquals(1, service.listarProductos(true).size());
        assertEquals(1, service.listarProductos(false).size());

        when(productoRepository.findById(1)).thenReturn(Optional.of(p));
        when(productoRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals("Aceite Lavanda", service.obtenerProductoPorId(1).getNombre());
        assertThrows(IllegalArgumentException.class, () -> service.obtenerProductoPorId(99));

        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Producto nuevo = producto(55, 4500, 3, true);
        nuevo.setNombre("  Vela  ");
        Producto creado = service.crearProducto(nuevo);
        assertNull(creado.getId());
        assertEquals("Vela", creado.getNombre());

        Producto datos = producto(null, 6000, 20, false);
        datos.setNombre("  Aceite Premium  ");
        Producto act = service.actualizarProducto(1, datos);
        assertEquals("Aceite Premium", act.getNombre());
        assertEquals(6000, act.getPrecio());
        assertFalse(act.isActivo());
    }

    @Test
    void serviceRegistrarCompraConsolidaLineasYValidaStock() {
        // Sin detalles
        Venta vacia = new Venta();
        assertThrows(IllegalArgumentException.class, () -> service.registrarCompra(vacia));

        // Detalle invalido
        Venta detalleInvalido = new Venta();
        detalleInvalido.setCompradorEmail("ana@sana.cl");
        detalleInvalido.setCompradorNombre("Ana");
        detalleInvalido.setMetodoPago("Tarjeta");
        DetalleVenta dBad = new DetalleVenta();
        dBad.setProductoId(1);
        dBad.setCantidad(0);
        detalleInvalido.setDetalles(List.of(dBad));
        assertThrows(IllegalArgumentException.class, () -> service.registrarCompra(detalleInvalido));

        // Producto inactivo
        Producto inactivo = producto(1, 5000, 10, false);
        when(productoRepository.findById(1)).thenReturn(Optional.of(inactivo));
        Venta vInactivo = new Venta();
        vInactivo.setCompradorEmail("ana@sana.cl");
        vInactivo.setCompradorNombre("Ana");
        vInactivo.setMetodoPago("Tarjeta");
        DetalleVenta d1 = new DetalleVenta();
        d1.setProductoId(1);
        d1.setCantidad(2);
        vInactivo.setDetalles(List.of(d1));
        assertThrows(IllegalArgumentException.class, () -> service.registrarCompra(vInactivo));

        // Stock insuficiente tras consolidar dos lineas del mismo producto
        Producto pocoStock = producto(1, 5000, 3, true);
        when(productoRepository.findById(1)).thenReturn(Optional.of(pocoStock));
        DetalleVenta d2 = new DetalleVenta();
        d2.setProductoId(1);
        d2.setCantidad(2);
        vInactivo.setDetalles(List.of(d1, d2)); // total 4 > 3
        assertThrows(IllegalArgumentException.class, () -> service.registrarCompra(vInactivo));

        // Compra exitosa consolidada
        Producto okProd = producto(1, 5000, 10, true);
        when(productoRepository.findById(1)).thenReturn(Optional.of(okProd));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any())).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(77);
            return v;
        });

        Venta guardada = service.registrarCompra(vInactivo);
        assertEquals(77, guardada.getId());
        assertEquals(20000, guardada.getTotal());
        assertEquals("PAGADA", guardada.getEstado());
        assertEquals(6, okProd.getStock());
        assertEquals(1, guardada.getDetalles().size());
        assertNotNull(guardada.getDetalles().get(0).getVenta());

        when(ventaRepository.findByCompradorEmailOrderByFechaMillisDesc("ana@sana.cl")).thenReturn(List.of(guardada));
        when(ventaRepository.findAllByOrderByFechaMillisDesc()).thenReturn(List.of(guardada));
        assertEquals(1, service.listarVentas("ANA@sana.cl").size());
        assertEquals(1, service.listarVentas(null).size());
    }

    @Test
    void controllerEndpointsYJwt() {
        Producto p = producto(1, 5000, 10, true);
        when(productoRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(p));
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));
        when(productoRepository.findById(99)).thenReturn(Optional.empty());
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(1, controller.listarProductos(true).getBody().size());
        assertEquals(HttpStatus.OK, controller.obtenerProducto(1).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerProducto(99).getStatusCode());
        assertEquals(HttpStatus.CREATED, controller.crearProducto(p).getStatusCode());
        assertEquals(HttpStatus.OK, controller.actualizarProducto(1, p).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.actualizarProducto(99, p).getStatusCode());

        Jwt pac = jwt("pac@sana.cl", "PACIENTE");
        Jwt admin = jwt("admin@sana.cl", "ADMIN");
        Venta vErr = new Venta();
        assertEquals(HttpStatus.BAD_REQUEST, controller.registrarVenta(vErr, pac).getStatusCode());

        Venta vOk = new Venta();
        vOk.setCompradorEmail("otro@sana.cl");
        vOk.setCompradorNombre("Pac");
        vOk.setMetodoPago("Tarjeta");
        DetalleVenta d = new DetalleVenta();
        d.setProductoId(1);
        d.setCantidad(1);
        vOk.setDetalles(List.of(d));
        when(ventaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var respVenta = controller.registrarVenta(vOk, pac);
        assertEquals(HttpStatus.CREATED, respVenta.getStatusCode());
        assertEquals("pac@sana.cl", ((Venta) respVenta.getBody()).getCompradorEmail());

        when(ventaRepository.findByCompradorEmailOrderByFechaMillisDesc("pac@sana.cl")).thenReturn(List.of(vOk));
        when(ventaRepository.findAllByOrderByFechaMillisDesc()).thenReturn(List.of(vOk));
        assertEquals(1, controller.listarVentas("otro@sana.cl", pac).getBody().size());
        assertEquals(1, controller.listarVentas(null, admin).getBody().size());
    }

    @Test
    void securityConfigJwtDecoderYRoles() {
        SecurityConfig config = new SecurityConfig();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(config, "jwtSecretBase64", secret);
        assertNotNull(config.jwtDecoder());
        var converter = config.jwtAuthenticationConverter();
        assertTrue(converter.convert(jwt("a@b.cl", "admin")).getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(converter.convert(jwt("a@b.cl", "")).getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
}
