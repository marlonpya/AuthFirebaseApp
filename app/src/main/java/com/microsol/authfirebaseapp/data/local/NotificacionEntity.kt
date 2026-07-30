package com.microsol.authfirebaseapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa una fila de la tabla "notificaciones".
 *
 * Vive en data/local (no en domain/) porque contiene anotaciones de Room (@Entity, @PrimaryKey):
 * domain/ debe seguir 100% Kotlin puro. La traducción a/desde el modelo de dominio (Notificacion)
 * ocurre en NotificacionMapper.
 *
 * [id] es autogenerado por Room (autoGenerate): al insertar se pasa 0 y Room asigna el siguiente id.
 */
@Entity(tableName = "notificaciones")
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val cuerpo: String,
    val fechaRecepcion: Long,
    val leida: Boolean = false
)
