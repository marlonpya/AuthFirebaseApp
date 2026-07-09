package com.microsol.authfirebaseapp.presentation.tareas.form

/**
 * Estado de una de las 3 casillas de foto del formulario de tarea. Vive en presentation/ (no
 * domain/), pero sin imports de Android: [Nueva.uriLocal] es el uri.toString() del Uri que
 * entrega el Photo Picker, no se sube a Storage hasta que se toca "Guardar".
 */
sealed class FotoSlot {
    object Vacio : FotoSlot()
    data class Existente(val url: String) : FotoSlot()
    data class Nueva(val uriLocal: String, val tipoContenido: String?) : FotoSlot()
}
