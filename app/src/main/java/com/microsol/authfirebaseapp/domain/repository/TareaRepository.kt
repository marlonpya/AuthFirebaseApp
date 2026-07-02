package com.microsol.authfirebaseapp.domain.repository

import com.microsol.authfirebaseapp.domain.model.Tarea

/**
 * Contrato del repositorio de tareas. Igual que CursoRepository, es una interfaz de domain/
 * implementada con Firestore en data/repository/FirestoreTareaRepositoryImpl.
 */
interface TareaRepository {
    /** Tareas de un curso concreto (filtro por FK cursoId, sin subcolecciones). */
    suspend fun obtenerTareasPorCurso(cursoId: String): List<Tarea>

    /** Alterna si la tarea está completada. */
    suspend fun actualizarCompletada(tareaId: String, completada: Boolean)

    /** Crea una tarea nueva asociada a [cursoId]. completada empieza en false. */
    suspend fun crearTarea(cursoId: String, titulo: String, fechaLimite: Long)

    /** Actualiza título/fechaLimite de una tarea existente. No modifica completada. */
    suspend fun actualizarTarea(tareaId: String, titulo: String, fechaLimite: Long)

    suspend fun eliminarTarea(tareaId: String)
}
