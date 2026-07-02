package com.microsol.authfirebaseapp.domain.model

/**
 * Modelo de dominio de un curso.
 *
 * Es un tipo 100% Kotlin puro: no importa nada de Android ni de Firebase/Firestore
 * (por eso [fechaCreacion] es un [Long] en epoch millis y no un Timestamp de Firestore).
 * La conversión entre el DTO de Firestore y este modelo vive en la capa data (CursoMapper).
 */
data class Curso(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaCreacion: Long
)
