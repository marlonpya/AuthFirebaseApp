package com.microsol.authfirebaseapp.presentation.tareas.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Factory manual: pasa cursoId (FK, siempre presente) y tareaId (vacío = modo creación). */
class TareaFormViewModelFactory(
    private val cursoId: String,
    private val tareaId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TareaFormViewModel(cursoId, tareaId) as T
    }
}
