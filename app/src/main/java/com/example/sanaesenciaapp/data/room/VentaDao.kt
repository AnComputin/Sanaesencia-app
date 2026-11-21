package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface VentaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarVenta(venta: VentaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarDetalles(detalles: List<DetalleVentaEntity>)
}
