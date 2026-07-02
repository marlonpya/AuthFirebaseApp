package com.microsol.authfirebaseapp.presentation.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.authfirebaseapp.data.repository.FirestoreTareaRepositoryImpl
import com.microsol.authfirebaseapp.domain.model.Tarea
import com.microsol.authfirebaseapp.domain.repository.TareaRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * ViewModel de TareasFragment: recibe [cursoId] (llegó a la pantalla por Safe Args) y el
 * repositorio por constructor. Igual que CursosViewModel, no conoce Fragment/View/Context y es
 * testeable en un test unitario JVM puro pasando un fake TareaRepository.
 *
 * No tiene un `init { cargarTareas() }` automático a propósito (igual que CursosViewModel):
 * TareasFragment llama a [cargarTareas] explícitamente en onViewCreated(), que se vuelve a
 * ejecutar al volver del formulario de creación/edición, así la lista queda siempre actualizada.
 */
class TareasViewModel(
    private val cursoId: String,
    private val repository: TareaRepository = FirestoreTareaRepositoryImpl()
) : ViewModel() {

    private val _estado = MutableStateFlow<TareasState>(TareasState.Loading)
    val estado: StateFlow<TareasState> = _estado.asStateFlow()

    fun cargarTareas() {
        viewModelScope.launch {
            _estado.value = TareasState.Loading
            try {
                val tareas = withTimeout(TIMEOUT_MS) { repository.obtenerTareasPorCurso(cursoId) }
                _estado.value = TareasState.Exito(tareas)
            } catch (e: TimeoutCancellationException) {
                _estado.value = TareasState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = TareasState.Error(e.message ?: "Error al cargar tareas")
            }
        }
    }

    /** Alterna "completada" y vuelve a cargar la lista. */
    fun alternarCompletada(tarea: Tarea) {
        viewModelScope.launch {
            try {
                withTimeout(TIMEOUT_MS) { repository.actualizarCompletada(tarea.id, !tarea.completada) }
                cargarTareas()
            } catch (e: TimeoutCancellationException) {
                _estado.value = TareasState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = TareasState.Error(e.message ?: "Error al actualizar la tarea")
            }
        }
    }

    /** Elimina la tarea y recarga la lista. */
    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            try {
                withTimeout(TIMEOUT_MS) { repository.eliminarTarea(tarea.id) }
                cargarTareas()
            } catch (e: TimeoutCancellationException) {
                _estado.value = TareasState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = TareasState.Error(e.message ?: "Error al eliminar la tarea")
            }
        }
    }

    private companion object {
        // Evita que la UI se quede "cargando" para siempre si el Task de Firestore nunca se
        // completa (p. ej. reintentos internos por un error de permisos/API deshabilitada).
        const val TIMEOUT_MS = 15_000L
        const val MENSAJE_TIMEOUT = "No se pudo conectar con Firestore (tiempo de espera agotado). " +
            "Revisa tu conexión o que Firestore esté habilitado en Firebase."
    }
}
