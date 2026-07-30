package com.microsol.authfirebaseapp.presentation.notificaciones

import com.microsol.authfirebaseapp.domain.model.Notificacion

/**
 * Estados posibles de NotificacionesFragment. Mismo patrón que CursosState/TareasState:
 * el caso "vacío" se maneja en la vista revisando Exito.notificaciones.isEmpty().
 */
sealed class NotificacionesState {
    object Loading : NotificacionesState()
    data class Exito(val notificaciones: List<Notificacion>) : NotificacionesState()
    data class Error(val mensaje: String) : NotificacionesState()
}
