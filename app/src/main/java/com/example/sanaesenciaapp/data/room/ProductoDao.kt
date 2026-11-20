package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE ASC")
    fun obtenerTodosFlow(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre COLLATE NOCASE ASC")
    fun obtenerActivosFlow(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(producto: ProductoEntity): Long

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarPorId(id: Int): Int

    @Query("UPDATE productos SET stock = :nuevoStock WHERE id = :id")
    suspend fun actualizarStock(id: Int, nuevoStock: Int): Int

    @Query(
        "UPDATE productos SET stock = stock - :cantidad " +
            "WHERE id = :id AND activo = 1 AND stock >= :cantidad"
    )
    suspend fun descontarStock(id: Int, cantidad: Int): Int
}
