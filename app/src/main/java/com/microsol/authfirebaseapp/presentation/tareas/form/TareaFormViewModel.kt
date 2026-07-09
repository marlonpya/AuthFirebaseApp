package com.microsol.authfirebaseapp.presentation.tareas.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.authfirebaseapp.data.repository.FirestoreTareaRepositoryImpl
import com.microsol.authfirebaseapp.data.repository.StorageRepositoryImpl
import com.microsol.authfirebaseapp.domain.repository.StorageRepository
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
 * Fragment/View/Context, solo expone StateFlow y recibe los repositorios por constructor.
 */
class TareaFormViewModel(
    private val cursoId: String,
    private val tareaId: String,
    private val repository: TareaRepository = FirestoreTareaRepositoryImpl(),
    private val storageRepository: StorageRepository = StorageRepositoryImpl()
) : ViewModel() {

    val esEdicion: Boolean get() = tareaId.isNotEmpty()

    private val _estado = MutableStateFlow<TareaFormState>(TareaFormState.Inactivo)
    val estado: StateFlow<TareaFormState> = _estado.asStateFlow()

    // Id de la tarea recién creada en un intento previo de guardar. Evita crear un documento
    // duplicado si una subida de foto falla y el usuario reintenta guardar (ver guardar()).
    private var idTareaCreada: String? = null

    /**
     * Guarda título/fecha y sube/borra las fotos de [slots] según corresponda, todo en la misma
     * operación: las fotos elegidas con el Photo Picker solo se suben a Storage al tocar
     * "Guardar" (no al seleccionarlas), así no quedan archivos huérfanos si el usuario abandona
     * el formulario. [urlsOriginales] son las URLs que ya estaban guardadas en Firestore antes de
     * abrir el formulario, para saber cuáles se quitaron y borrarlas de Storage.
     */
    fun guardar(
        titulo: String,
        fechaLimite: Long?,
        slots: List<FotoSlot>,
        urlsOriginales: List<String>
    ) {
        if (titulo.isBlank() || fechaLimite == null) {
            _estado.value = TareaFormState.Error(MENSAJE_CAMPOS_VACIOS)
            return
        }

        viewModelScope.launch {
            _estado.value = TareaFormState.Guardando
            try {
                withTimeout(TIMEOUT_MS) {
                    val idTarea = when {
                        esEdicion -> tareaId
                        idTareaCreada != null -> idTareaCreada!!
                        else -> repository.crearTarea(cursoId, titulo, fechaLimite)
                            .also { idTareaCreada = it }
                    }
                    if (esEdicion) {
                        repository.actualizarTarea(tareaId, titulo, fechaLimite)
                    }

                    val urlsFinales = slots.mapNotNull { slot ->
                        when (slot) {
                            is FotoSlot.Existente -> slot.url
                            is FotoSlot.Nueva -> storageRepository.subirFotoTarea(
                                idTarea,
                                slot.uriLocal,
                                slot.tipoContenido
                            )
                            FotoSlot.Vacio -> null
                        }
                    }

                    (urlsOriginales - urlsFinales.toSet()).forEach {
                        storageRepository.eliminarFotoTarea(it)
                    }
                    repository.actualizarImagenesUrls(idTarea, urlsFinales)
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
