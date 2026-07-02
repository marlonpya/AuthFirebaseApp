package com.microsol.authfirebaseapp.domain.repository

import com.microsol.authfirebaseapp.domain.model.Curso

/**
 * Contrato del repositorio de cursos. Vive en domain/ como interfaz para que el ViewModel
 * dependa solo de esta abstracción y nunca de Firestore directamente (inversión de dependencias).
 * La implementación real (con Firestore) está en data/repository/FirestoreCursoRepositoryImpl.
 */
interface CursoRepository {
    suspend fun obtenerCursos(): List<Curso>

    /** Crea un curso nuevo; el id y fechaCreacion los asigna Firestore (ver implementación). */
    suspend fun crearCurso(nombre: String, descripcion: String)

    /** Actualiza nombre/descripcion de un curso existente. No modifica fechaCreacion. */
    suspend fun actualizarCurso(cursoId: String, nombre: String, descripcion: String)

    /** Elimina el curso y, en cascada, todas sus tareas (evita huérfanos con cursoId inválido). */
    suspend fun eliminarCurso(cursoId: String)
}
