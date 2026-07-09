package com.microsol.authfirebaseapp.domain.repository

/**
 * Contrato de Firebase Storage para las fotos adjuntas a una tarea. Tipo 100% Kotlin puro: sin
 * imports de Android ni de Firebase (igual criterio que fechaLimite: Long en vez de Timestamp).
 * [uriOrigen] es el uri.toString() del Uri de Android que entrega el Photo Picker; la conversión
 * de vuelta a Uri ocurre solo en data/repository/StorageRepositoryImpl.
 */
interface StorageRepository {

    /**
     * Sube la imagen apuntada por [uriOrigen] a la carpeta de [tareaId] con un nombre único (no
     * pisa fotos previas de la misma tarea) y devuelve la URL de descarga HTTPS. [tipoContenido]
     * es el MIME type reportado por ContentResolver; si es null se asume "image/jpeg".
     */
    suspend fun subirFotoTarea(tareaId: String, uriOrigen: String, tipoContenido: String?): String

    /** Borra un objeto de Storage a partir de su URL de descarga. */
    suspend fun eliminarFotoTarea(url: String)

    /** Borra todas las fotos de una tarea (usado al eliminar la tarea completa). */
    suspend fun eliminarFotosTarea(tareaId: String)
}
