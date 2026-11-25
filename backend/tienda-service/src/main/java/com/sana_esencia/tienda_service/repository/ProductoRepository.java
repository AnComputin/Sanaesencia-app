package com.sana_esencia.tienda_service.repository;

import com.sana_esencia.tienda_service.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByActivoTrueOrderByNombreAsc();
    List<Producto> findAllByOrderByNombreAsc();
}