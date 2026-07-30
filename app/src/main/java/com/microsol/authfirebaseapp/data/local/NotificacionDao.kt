package com.microsol.authfirebaseapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) de Room: declara las operaciones sobre la tabla "notificaciones".
 * Room genera la implementación en tiempo de compilación (por eso se necesita KSP + room-compiler).
 *
 * [observarTodas] devuelve un [Flow]: Room reemite la lista completa cada vez que la tabla cambia
 * (nueva notificación insertada, una marcada como leída o eliminada), así la UI se actualiza sola.
 * Las funciones de escritura son `suspend` para ejecutarse fuera del hilo principal con corrutinas.
 */
@Dao
interface NotificacionDao {

    @Query("SELECT * FROM notificaciones ORDER BY fechaRecepcion DESC")
    fun observarTodas(): Flow<List<NotificacionEntity>>

    @Insert
    suspend fun insertar(notificacion: NotificacionEntity): Long

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: Long)

    @Query("DELETE FROM notificaciones WHERE id = :id")
    suspend fun eliminar(id: Long)
}
