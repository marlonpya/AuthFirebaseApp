package com.microsol.authfirebaseapp.presentation.cursos.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Factory manual: pasa cursoId (recibido por Safe Args; vacío = modo creación) al ViewModel. */
class CursoFormViewModelFactory(private val cursoId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CursoFormViewModel(cursoId) as T
    }
}
