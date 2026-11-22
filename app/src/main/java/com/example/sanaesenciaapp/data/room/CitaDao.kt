package com.example.sanaesenciaapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sanaesenciaapp.data.repository.ReglasAgenda
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CitaDao {

    @Query("SELECT * FROM citas ORDER BY inicioMillis ASC")
    abstract fun obtenerTodasFlow(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas WHERE usuarioEmail = :email ORDER BY inicioMillis ASC")
    abstract fun obtenerPorUsuarioFlow(email: String): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas ORDER BY inicioMillis ASC")
    abstract suspend fun obtenerTodas(): List<CitaEntity>

    @Query(
        """
        SELECT COUNT(*) FROM citas
        WHERE especialistaId = :especialistaId
          AND estado IN ('CONFIRMADA', 'EN_PAGO')
          AND inicioMillis < :finMillis
          AND finMillis > :inicioMillis
        """
    )
    protected abstract suspend fun contarSolapesEspecialista(
        especialistaId: Int,
        inicioMillis: Long,
        finMillis: Long
    ): Int

    @Query(
        """
        SELECT COUNT(*) FROM citas
        WHERE modalidad = 'PRESENCIAL'
          AND estado IN ('CONFIRMADA', 'EN_PAGO')
          AND inicioMillis < :finMillis
          AND finMillis > :inicioMillis
        """
    )
    protected abstract suspend fun contarPresencialesSolapadas(
        inicioMillis: Long,
        finMillis: Long
    ): Int

    @Query(
        """
        SELECT boxAsignado FROM citas
        WHERE modalidad = 'PRESENCIAL'
          AND estado IN ('CONFIRMADA', 'EN_PAGO')
          AND boxAsignado IS NOT NULL
          AND inicioMillis < :finMillis
          AND finMillis > :inicioMillis
        """
    )
    protected abstract suspend fun obtenerBoxesOcupados(
        inicioMillis: Long,
        finMillis: Long
    ): List<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertarInterno(cita: CitaEntity): Long

    @Query("UPDATE citas SET estado = :nuevoEstado WHERE id = :id")
    abstract suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    /*elimina una retencion en pago solo si sigue en estado EN_PAGO*/
    @Query("DELETE FROM citas WHERE id = :id AND estado = 'EN_PAGO'")
    abstract suspend fun eliminar(id: Int)

    /*limpia retenciones huerfanas (pago abandonado o app cerrada)*/
    @Query("DELETE FROM citas WHERE estado = 'EN_PAGO'")
    abstract suspend fun eliminarRetenciones()

    /**
     * Confirma una cita de forma atómica.
     * - Nunca permite dos citas superpuestas para el mismo especialista.
     * - Una cita presencial necesita uno de los 8 boxes libres.
     * - Una cita online no consume box.
     *
     * Retorna la cita ya guardada (con id y box) o null si el horario dejó de estar disponible.
     */
    @Transaction
    open suspend fun reservarSiDisponible(cita: CitaEntity): CitaEntity? {
        if (
            contarSolapesEspecialista(
                especialistaId = cita.especialistaId,
                inicioMillis = cita.inicioMillis,
                finMillis = cita.finMillis
            ) > 0
        ) {
            return null
        }

        val boxAsignado = if (ReglasAgenda.esPresencial(cita.modalidad)) {
            if (
                contarPresencialesSolapadas(
                    inicioMillis = cita.inicioMillis,
                    finMillis = cita.finMillis
                ) >= ReglasAgenda.TOTAL_BOXES
            ) {
                return null
            }

            val ocupados = obtenerBoxesOcupados(
                inicioMillis = cita.inicioMillis,
                finMillis = cita.finMillis
            ).toSet()

            ReglasAgenda.obtenerBoxLibre(ocupados) ?: return null
        } else {
            null
        }

        val citaAInsertar = cita.copy(boxAsignado = boxAsignado)
        val idGenerado = insertarInterno(citaAInsertar)

        return citaAInsertar.copy(id = idGenerado.toInt())
    }
}
