package com.microsol.authfirebaseapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * DTO que refleja exactamente el documento de la colección "cursos" en Firestore.
 * Los valores por defecto y @DocumentId son necesarios para que
 * DocumentSnapshot.toObject/toObjects() pueda deserializarlo automáticamente.
 */
data class CursoDto(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val fechaCreacion: Timestamp? = null
)
