package com.microsol.authfirebaseapp.data.mapper

import com.microsol.authfirebaseapp.data.model.CursoDto
import com.microsol.authfirebaseapp.domain.model.Curso

/**
 * Traduce entre el DTO de Firestore (CursoDto) y el modelo de dominio (Curso).
 * Vive en data/ (no en domain/) para que domain nunca importe com.google.firebase.Timestamp:
 * aquí es donde ocurre la conversión Timestamp -> Long (epoch millis).
 */
object CursoMapper {
    fun toDomain(dto: CursoDto) = Curso(
        id = dto.id,
        nombre = dto.nombre,
        descripcion = dto.descripcion,
        fechaCreacion = dto.fechaCreacion?.toDate()?.time ?: 0L
    )
}
