package com.microsol.authfirebaseapp.presentation.cursos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Factory manual: el proyecto no usa Hilt/Koin, así que se crea el ViewModel a mano. */
class CursosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CursosViewModel() as T
    }
}
