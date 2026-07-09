package com.microsol.authfirebaseapp.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.microsol.authfirebaseapp.domain.repository.StorageRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Implementación de StorageRepository con Firebase Storage. Ruta con subcarpeta por tarea
 * (tareas/{tareaId}/{uuid}.jpg): "tareas" es un catálogo colaborativo sin dueño (igual que
 * firestore.rules), y cada foto lleva un nombre único para no pisar ni dejar huérfanas las demás
 * fotos de la misma tarea al reemplazar o quitar una sola.
 */
class StorageRepositoryImpl(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : StorageRepository {

    override suspend fun subirFotoTarea(
        tareaId: String,
        uriOrigen: String,
        tipoContenido: String?
    ): String {
        val referencia = storage.reference.child(
            "$CARPETA_TAREAS/$tareaId/${UUID.randomUUID()}$EXTENSION"
        )
        val metadatos = StorageMetadata.Builder()
            .setContentType(tipoContenido ?: TIPO_CONTENIDO_POR_DEFECTO)
            .build()
        referencia.putFile(Uri.parse(uriOrigen), metadatos).await()
        return referencia.downloadUrl.await().toString()
    }

    override suspend fun eliminarFotoTarea(url: String) {
        storage.getReferenceFromUrl(url).delete().await()
    }

    override suspend fun eliminarFotosTarea(tareaId: String) {
        val carpeta = storage.reference.child("$CARPETA_TAREAS/$tareaId")
        carpeta.listAll().await().items.forEach { it.delete().await() }
    }

    private companion object {
        const val CARPETA_TAREAS = "tareas"
        const val EXTENSION = ".jpg"
        const val TIPO_CONTENIDO_POR_DEFECTO = "image/jpeg"
    }
}
