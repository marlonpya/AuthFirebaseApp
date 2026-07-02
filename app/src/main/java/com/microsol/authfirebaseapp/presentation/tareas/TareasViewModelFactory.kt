package com.microsol.authfirebaseapp.presentation.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Factory manual: pasa cursoId (recibido por Safe Args en el Fragment) al ViewModel. */
class TareasViewModelFactory(private val cursoId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TareasViewModel(cursoId) as T
    }
}
