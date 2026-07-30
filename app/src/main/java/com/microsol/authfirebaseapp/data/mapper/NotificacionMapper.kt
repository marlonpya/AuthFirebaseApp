package com.microsol.authfirebaseapp.data.mapper

import com.microsol.authfirebaseapp.data.local.NotificacionEntity
import com.microsol.authfirebaseapp.domain.model.Notificacion

/**
 * Traduce entre la entidad de Room (NotificacionEntity) y el modelo de dominio (Notificacion).
 * Vive en data/ (no en domain/) para que domain nunca importe nada de Room: aquí es el único punto
 * donde se conocen ambos tipos. La conversión es directa (mismos campos) porque no hay tipos
 * específicos de Room que traducir, pero mantener el mapper deja la puerta abierta a que diverjan.
 */
object NotificacionMapper {
    fun toDomain(entity: NotificacionEntity) = Notificacion(
        id = entity.id,
        titulo = entity.titulo,
        cuerpo = entity.cuerpo,
        fechaRecepcion = entity.fechaRecepcion,
        leida = entity.leida
    )
}
