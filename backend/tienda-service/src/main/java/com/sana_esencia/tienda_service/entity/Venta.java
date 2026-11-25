package com.sana_esencia.tienda_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ventas",
        indexes = { @Index(name = "idx_ventas_comprador", columnList = "comprador_email") }
)
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El correo del comprador es obligatorio")
    @Column(name = "comprador_email", nullable = false, length = 150)
    private String compradorEmail;

    @NotBlank(message = "El nombre del comprador es obligatorio")
    @Column(name = "comprador_nombre", nullable = false, length = 120)
    private String compradorNombre;

    @Column(name = "fecha_millis", nullable = false)
    private Long fechaMillis;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false, length = 30)
    private String estado = "PAGADA";

    @NotBlank(message = "El método de pago es obligatorio")
    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {}

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCompradorEmail() { return compradorEmail; }
    public void setCompradorEmail(String compradorEmail) { this.compradorEmail = compradorEmail; }
    public String getCompradorNombre() { return compradorNombre; }
    public void setCompradorNombre(String compradorNombre) { this.compradorNombre = compradorNombre; }
    public Long getFechaMillis() { return fechaMillis; }
    public void setFechaMillis(Long fechaMillis) { this.fechaMillis = fechaMillis; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}