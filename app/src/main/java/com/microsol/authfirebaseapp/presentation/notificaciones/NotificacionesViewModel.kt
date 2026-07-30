package com.microsol.authfirebaseapp.presentation.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.authfirebaseapp.domain.model.Notificacion
import com.microsol.authfirebaseapp.domain.repository.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel de NotificacionesFragment. Como los demás ViewModels del proyecto, no conoce
 * Fragment/View/Context: solo expone [estado] como StateFlow y recibe el repositorio por constructor.
 *
 * A diferencia de CursosViewModel, aquí NO hay valor por defecto en el constructor (construir Room
 * necesita un Context) ni un método `cargar()` que se llame desde el Fragment: en su lugar, en el
 * [init] nos suscribimos al Flow del repositorio, de modo que la lista se actualiza sola cuando
 * llega una notificación nueva, se marca como leída o se elimina.
 */
class NotificacionesViewModel(
    private val repository: NotificacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<NotificacionesState>(NotificacionesState.Loading)
    val estado: StateFlow<NotificacionesState> = _estado.asStateFlow()

    init {
        observarNotificaciones()
    }

    private fun observarNotificaciones() {
        viewModelScope.launch {
            repository.observarNotificaciones()
                .catch { e ->
                    _estado.value = NotificacionesState.Error(
                        e.message ?: "Error al leer las notificaciones guardadas"
                    )
                }
                .collect { lista ->
                    _estado.value = NotificacionesState.Exito(lista)
                }
        }
    }

    /** Marca la notificación como leída. Room reemite la lista y la UI se refresca sola. */
    fun marcarComoLeida(notificacion: Notificacion) {
        // Si ya está leída no hace falta escribir en la base de datos.
        if (notificacion.leida) return
        viewModelScope.launch {
            try {
                repository.marcarComoLeida(notificacion.id)
            } catch (e: Exception) {
                _estado.value = NotificacionesState.Error(
                    e.message ?: "No se pudo marcar como leída"
                )
            }
        }
    }

    /** Elimina la notificación de forma permanente. Room reemite la lista sin ese elemento. */
    fun eliminar(notificacion: Notificacion) {
        viewModelScope.launch {
            try {
                repository.eliminar(notificacion.id)
            } catch (e: Exception) {
                _estado.value = NotificacionesState.Error(
                    e.message ?: "No se pudo eliminar la notificación"
                )
            }
        }
    }
}
