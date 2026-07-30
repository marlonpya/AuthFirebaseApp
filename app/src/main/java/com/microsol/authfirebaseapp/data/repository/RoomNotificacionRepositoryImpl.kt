package com.microsol.authfirebaseapp.data.repository

import com.microsol.authfirebaseapp.data.local.NotificacionDao
import com.microsol.authfirebaseapp.data.local.NotificacionEntity
import com.microsol.authfirebaseapp.data.mapper.NotificacionMapper
import com.microsol.authfirebaseapp.domain.model.Notificacion
import com.microsol.authfirebaseapp.domain.repository.NotificacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de NotificacionRepository con Room. Recibe el [NotificacionDao] por constructor;
 * quien la crea (NotificacionesViewModelFactory o AppMessagingService) lo obtiene de
 * AppDatabase.getInstance(context). A diferencia de FirestoreCursoRepositoryImpl no tiene un valor
 * por defecto en el constructor, porque construir la base de datos necesita un Context.
 */
class RoomNotificacionRepositoryImpl(
    private val dao: NotificacionDao
) : NotificacionRepository {

    // map (de Flow) traduce cada emisión de la lista de entidades a la lista de modelos de dominio,
    // manteniendo la reactividad: si la tabla cambia, Room reemite y el ViewModel recibe la lista nueva.
    override fun observarNotificaciones(): Flow<List<Notificacion>> =
        dao.observarTodas().map { entidades -> entidades.map(NotificacionMapper::toDomain) }

    override suspend fun guardar(titulo: String, cuerpo: String, fechaRecepcion: Long) {
        // id = 0 → Room autogenera el id real (ver NotificacionEntity).
        dao.insertar(
            NotificacionEntity(
                titulo = titulo,
                cuerpo = cuerpo,
                fechaRecepcion = fechaRecepcion
            )
        )
    }

    override suspend fun marcarComoLeida(id: Long) = dao.marcarComoLeida(id)

    override suspend fun eliminar(id: Long) = dao.eliminar(id)
}
