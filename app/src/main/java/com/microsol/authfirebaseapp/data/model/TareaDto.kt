package com.microsol.authfirebaseapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/** DTO que refleja exactamente el documento de la colección "tareas" en Firestore. */
data class TareaDto(
    @DocumentId val id: String = "",
    val cursoId: String = "",
    val titulo: String = "",
    val completada: Boolean = false,
    val fechaLimite: Timestamp? = null
)
