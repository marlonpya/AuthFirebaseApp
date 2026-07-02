package com.microsol.authfirebaseapp.presentation.tareas.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.authfirebaseapp.data.repository.FirestoreTareaRepositoryImpl
import com.microsol.authfirebaseapp.domain.repository.TareaRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * ViewModel de TareaFormFragment: sirve tanto para crear como para editar una tarea de [cursoId].
 * [tareaId] vacío significa modo creación; no vacío significa modo edición. No conoce
 * Fragment/View/Context, solo expone StateFlow y recibe el repositorio por constructor.
 */
class TareaFormViewModel(
    private val cursoId: String,
    private val tareaId: String,
    private val repository: TareaRepository = FirestoreTareaRepositoryImpl()
) : ViewModel() {

    val esEdicion: Boolean get() = tareaId.isNotEmpty()

    private val _estado = MutableStateFlow<TareaFormState>(TareaFormState.Inactivo)
    val estado: StateFlow<TareaFormState> = _estado.asStateFlow()

    fun guardar(titulo: String, fechaLimite: Long?) {
        if (titulo.isBlank() || fechaLimite == null) {
            _estado.value = TareaFormState.Error(MENSAJE_CAMPOS_VACIOS)
            return
        }

        viewModelScope.launch {
            _estado.value = TareaFormState.Guardando
            try {
                withTimeout(TIMEOUT_MS) {
                    if (esEdicion) {
                        repository.actualizarTarea(tareaId, titulo, fechaLimite)
                    } else {
                        repository.crearTarea(cursoId, titulo, fechaLimite)
                    }
                }
                _estado.value = TareaFormState.Guardado
            } catch (e: TimeoutCancellationException) {
                _estado.value = TareaFormState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = TareaFormState.Error(e.message ?: "Error al guardar la tarea")
            }
        }
    }

    private companion object {
        const val MENSAJE_CAMPOS_VACIOS = "Completa el título y la fecha límite"
        const val TIMEOUT_MS = 15_000L
        const val MENSAJE_TIMEOUT = "No se pudo conectar con Firestore (tiempo de espera agotado). " +
            "Revisa tu conexión o que Firestore esté habilitado en Firebase."
    }
}
