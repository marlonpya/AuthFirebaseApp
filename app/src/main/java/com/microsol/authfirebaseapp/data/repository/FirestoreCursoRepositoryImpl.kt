package com.microsol.authfirebaseapp.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.microsol.authfirebaseapp.data.FirestoreColecciones
import com.microsol.authfirebaseapp.data.mapper.CursoMapper
import com.microsol.authfirebaseapp.data.model.CursoDto
import com.microsol.authfirebaseapp.domain.model.Curso
import com.microsol.authfirebaseapp.domain.repository.CursoRepository
import kotlinx.coroutines.tasks.await

/**
 * Implementación de CursoRepository con Firestore.
 * "cursos" es un catálogo global (sin campo de propietario): cualquier usuario autenticado
 * puede leer y escribir todos los cursos, tal como definen las reglas de seguridad
 * (firestore.rules).
 */
class FirestoreCursoRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CursoRepository {

    override suspend fun obtenerCursos(): List<Curso> {
        val snapshot = db.collection(FirestoreColecciones.CURSOS).get().await()
        return snapshot.toObjects(CursoDto::class.java).map(CursoMapper::toDomain)
    }

    override suspend fun crearCurso(nombre: String, descripcion: String) {
        val datos = mapOf(
            CAMPO_NOMBRE to nombre,
            CAMPO_DESCRIPCION to descripcion,
            // Timestamp del servidor: evita depender del reloj del dispositivo.
            CAMPO_FECHA_CREACION to FieldValue.serverTimestamp()
        )
        db.collection(FirestoreColecciones.CURSOS).add(datos).await()
    }

    override suspend fun actualizarCurso(cursoId: String, nombre: String, descripcion: String) {
        val datos = mapOf(
            CAMPO_NOMBRE to nombre,
            CAMPO_DESCRIPCION to descripcion
        )
        db.collection(FirestoreColecciones.CURSOS).document(cursoId).update(datos).await()
    }

    /**
     * Borrado en cascada: elimina el curso y todas las tareas cuyo cursoId apunte a él, en un
     * único WriteBatch (atómico) para no dejar tareas huérfanas apuntando a un curso inexistente.
     */
    override suspend fun eliminarCurso(cursoId: String) {
        val tareasDelCurso = db.collection(FirestoreColecciones.TAREAS)
            .whereEqualTo(CAMPO_CURSO_ID, cursoId)
            .get()
            .await()

        val batch = db.batch()
        tareasDelCurso.documents.forEach { batch.delete(it.reference) }
        batch.delete(db.collection(FirestoreColecciones.CURSOS).document(cursoId))
        batch.commit().await()
    }

    private companion object {
        const val CAMPO_NOMBRE = "nombre"
        const val CAMPO_DESCRIPCION = "descripcion"
        const val CAMPO_FECHA_CREACION = "fechaCreacion"
        const val CAMPO_CURSO_ID = "cursoId"
    }
}
