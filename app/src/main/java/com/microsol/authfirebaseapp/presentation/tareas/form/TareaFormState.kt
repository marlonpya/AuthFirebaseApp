package com.microsol.authfirebaseapp.presentation.tareas.form

/** Estados posibles de TareaFormFragment (pantalla de crear/editar una tarea). */
sealed class TareaFormState {
    object Inactivo : TareaFormState()
    object Guardando : TareaFormState()
    object Guardado : TareaFormState()
    data class Error(val mensaje: String) : TareaFormState()
}
