package com.microsol.authfirebaseapp.presentation.notificaciones

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.microsol.authfirebaseapp.data.local.AppDatabase
import com.microsol.authfirebaseapp.data.repository.RoomNotificacionRepositoryImpl

/**
 * Factory manual (el proyecto no usa Hilt/Koin). A diferencia de CursosViewModelFactory, necesita
 * un [Context] para construir la base de datos Room: obtiene el DAO del singleton AppDatabase y con
 * él arma el RoomNotificacionRepositoryImpl que le pasa al ViewModel. Usa el applicationContext
 * (dentro de getInstance) para no filtrar la Activity/Fragment.
 */
class NotificacionesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dao = AppDatabase.getInstance(context).notificacionDao()
        val repository = RoomNotificacionRepositoryImpl(dao)
        @Suppress("UNCHECKED_CAST")
        return NotificacionesViewModel(repository) as T
    }
}
