package com.example.sanaesenciaapp.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val compradorEmail: String,
    val compradorNombre: String,
    val fechaMillis: Long,
    val total: Int,
    val estado: String,
    val metodoPago: String
)

@Entity(
    tableName = "detalle_ventas",
    foreignKeys = [
        ForeignKey(
            entity = VentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ventaId"])]
)
data class DetalleVentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ventaId: Int,
    val productoId: Int,
    val nombreProducto: String,
    val precioUnitario: Int,
    val cantidad: Int,
    val subtotal: Int
)
