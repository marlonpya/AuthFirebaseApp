package com.microsol.authfirebaseapp.presentation.cursos.form

/** Estados posibles de CursoFormFragment (pantalla de crear/editar un curso). */
sealed class CursoFormState {
    object Inactivo : CursoFormState()
    object Guardando : CursoFormState()
    object Guardado : CursoFormState()
    data class Error(val mensaje: String) : CursoFormState()
}
