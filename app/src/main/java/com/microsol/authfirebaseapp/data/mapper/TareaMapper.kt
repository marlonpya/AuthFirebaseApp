package com.microsol.authfirebaseapp.data.mapper

import com.microsol.authfirebaseapp.data.model.TareaDto
import com.microsol.authfirebaseapp.domain.model.Tarea

/** Traduce entre TareaDto (Firestore) y Tarea (dominio). Ver CursoMapper para el porqué de su ubicación. */
object TareaMapper {
    fun toDomain(dto: TareaDto) = Tarea(
        id = dto.id,
        cursoId = dto.cursoId,
        titulo = dto.titulo,
        completada = dto.completada,
        fechaLimite = dto.fechaLimite?.toDate()?.time ?: 0L
    )
}
