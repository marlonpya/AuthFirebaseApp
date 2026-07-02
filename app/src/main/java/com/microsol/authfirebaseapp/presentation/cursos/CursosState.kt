package com.microsol.authfirebaseapp.presentation.cursos

import com.microsol.authfirebaseapp.domain.model.Curso

/** Estados posibles de CursosFragment. El caso "vacío" se maneja en la vista revisando Exito.cursos.isEmpty(). */
sealed class CursosState {
    object Loading : CursosState()
    data class Exito(val cursos: List<Curso>) : CursosState()
    data class Error(val mensaje: String) : CursosState()
}
