package com.microsol.authfirebaseapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.microsol.authfirebaseapp.data.FirestoreColecciones
import com.microsol.authfirebaseapp.data.mapper.TareaMapper
import com.microsol.authfirebaseapp.data.model.TareaDto
import com.microsol.authfirebaseapp.domain.model.Tarea
import com.microsol.authfirebaseapp.domain.repository.TareaRepository
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Implementación de TareaRepository con Firestore.
 * "tareas" es una colección raíz (no subcolección): se filtra por la FK cursoId con whereEqualTo.
 */
class FirestoreTareaRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TareaRepository {

    override suspend fun obtenerTareasPorCurso(cursoId: String): List<Tarea> {
        val snapshot = db.collection(FirestoreColecciones.TAREAS)
            .whereEqualTo(CAMPO_CURSO_ID, cursoId)
            .get()
            .await()
        return snapshot.toObjects(TareaDto::class.java).map(TareaMapper::toDomain)
    }

    override suspend fun actualizarCompletada(tareaId: String, completada: Boolean) {
        db.collection(FirestoreColecciones.TAREAS)
            .document(tareaId)
            .update(CAMPO_COMPLETADA, completada)
            .await()
    }

    override suspend fun crearTarea(cursoId: String, titulo: String, fechaLimite: Long) {
        val datos = mapOf(
            CAMPO_CURSO_ID to cursoId,
            CAMPO_TITULO to titulo,
            CAMPO_COMPLETADA to false,
            // fechaLimite la elige el usuario (no "ahora"), por eso se construye un Timestamp
            // explícito en vez de FieldValue.serverTimestamp() (que sí se usa para fechaCreacion).
            CAMPO_FECHA_LIMITE to Timestamp(Date(fechaLimite))
        )
        db.collection(FirestoreColecciones.TAREAS).add(datos).await()
    }

    override suspend fun actualizarTarea(tareaId: String, titulo: String, fechaLimite: Long) {
        val datos = mapOf(
            CAMPO_TITULO to titulo,
            CAMPO_FECHA_LIMITE to Timestamp(Date(fechaLimite))
        )
        db.collection(FirestoreColecciones.TAREAS).document(tareaId).update(datos).await()
    }

    override suspend fun eliminarTarea(tareaId: String) {
        db.collection(FirestoreColecciones.TAREAS).document(tareaId).delete().await()
    }

    private companion object {
        const val CAMPO_CURSO_ID = "cursoId"
        const val CAMPO_COMPLETADA = "completada"
        const val CAMPO_TITULO = "titulo"
        const val CAMPO_FECHA_LIMITE = "fechaLimite"
    }
}
