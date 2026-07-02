package com.microsol.authfirebaseapp.domain.model

/**
 * Modelo de dominio de una tarea. [cursoId] es la FK plana hacia [Curso.id]
 * (Firestore no usa subcolecciones aquí: tareas es una colección raíz filtrada por cursoId).
 *
 * Tipo 100% Kotlin puro: sin imports de Android ni de Firebase (ver Curso.kt).
 */
data class Tarea(
    val id: String,
    val cursoId: String,
    val titulo: String,
    val completada: Boolean,
    val fechaLimite: Long
)
