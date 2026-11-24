// repository/VentaRepository.java
package com.sana_esencia.tienda_service.repository;

import com.sana_esencia.tienda_service.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByCompradorEmailOrderByFechaMillisDesc(String compradorEmail);
}