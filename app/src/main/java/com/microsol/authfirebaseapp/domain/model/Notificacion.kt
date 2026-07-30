package com.microsol.authfirebaseapp.domain.model

/**
 * Modelo de dominio de una notificación recibida por Firebase Cloud Messaging (FCM) y guardada
 * localmente con Room.
 *
 * Igual que [Curso] y [Tarea], es un tipo 100% Kotlin puro: no importa nada de Android ni de Room
 * (por eso [fechaRecepcion] es un [Long] en epoch millis y no un tipo específico de la base de
 * datos). La conversión entre la entidad de Room (NotificacionEntity) y este modelo vive en la
 * capa data (NotificacionMapper).
 */
data class Notificacion(
    val id: Long,
    val titulo: String,
    val cuerpo: String,
    /** Momento en que llegó la notificación, en milisegundos epoch. */
    val fechaRecepcion: Long,
    /** true si el usuario ya la abrió (se muestra en gris/normal); false = sin leer (en negrita). */
    val leida: Boolean
)
