package com.microsol.authfirebaseapp.domain.repository

import com.microsol.authfirebaseapp.domain.model.Notificacion
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de notificaciones. Vive en domain/ como interfaz para que el ViewModel
 * (y el servicio de mensajería que persiste las notificaciones) dependan solo de esta abstracción
 * y nunca de Room directamente. La implementación real (con Room) está en
 * data/repository/RoomNotificacionRepositoryImpl.
 *
 * A diferencia de CursoRepository/TareaRepository (que hacen `suspend fun obtener...(): List<...>`
 * y se recargan a mano), aquí [observarNotificaciones] devuelve un [Flow]: como las notificaciones
 * pueden llegar en cualquier momento (incluso con la pantalla abierta), Room emite la lista
 * actualizada automáticamente y la UI se refresca sola.
 */
interface NotificacionRepository {

    /** Flujo con todas las notificaciones guardadas, de la más reciente a la más antigua. */
    fun observarNotificaciones(): Flow<List<Notificacion>>

    /** Guarda una notificación recién recibida (lo llama AppMessagingService.onMessageReceived). */
    suspend fun guardar(titulo: String, cuerpo: String, fechaRecepcion: Long)

    /** Marca una notificación como leída (al tocarla en la lista). */
    suspend fun marcarComoLeida(id: Long)

    /** Elimina una notificación de forma permanente. */
    suspend fun eliminar(id: Long)
}
