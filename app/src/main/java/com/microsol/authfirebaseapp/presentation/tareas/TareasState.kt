package com.microsol.authfirebaseapp.presentation.tareas

import com.microsol.authfirebaseapp.domain.model.Tarea

/** Estados posibles de TareasFragment. Igual que CursosState: "vacío" se revisa con Exito.tareas.isEmpty(). */
sealed class TareasState {
    object Loading : TareasState()
    data class Exito(val tareas: List<Tarea>) : TareasState()
    data class Error(val mensaje: String) : TareasState()
}
