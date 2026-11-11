package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeneficiarioDao {

    @Query(
        """
        SELECT * FROM beneficiarios
        WHERE propietarioEmail = :email AND activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
        """
    )
    fun obtenerPorPropietarioFlow(email: String): Flow<List<BeneficiarioEntity>>

    @Query(
        """
        SELECT * FROM beneficiarios
        WHERE propietarioEmail = :email AND activo = 1
        ORDER BY nombre COLLATE NOCASE ASC
        """
    )
    suspend fun obtenerPorPropietario(email: String): List<BeneficiarioEntity>

    @Query("SELECT * FROM beneficiarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): BeneficiarioEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(beneficiario: BeneficiarioEntity): Long

    @Query("UPDATE beneficiarios SET activo = 0 WHERE id = :id AND propietarioEmail = :email")
    suspend fun desactivar(id: Int, email: String)
}
