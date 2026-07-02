package com.microsol.authfirebaseapp.presentation.cursos.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.authfirebaseapp.data.repository.FirestoreCursoRepositoryImpl
import com.microsol.authfirebaseapp.domain.repository.CursoRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * ViewModel de CursoFormFragment: sirve tanto para crear como para editar un curso.
 * [cursoId] vacío significa modo creación; no vacío significa modo edición. No conoce
 * Fragment/View/Context, solo expone StateFlow y recibe el repositorio por constructor.
 */
class CursoFormViewModel(
    private val cursoId: String,
    private val repository: CursoRepository = FirestoreCursoRepositoryImpl()
) : ViewModel() {

    val esEdicion: Boolean get() = cursoId.isNotEmpty()

    private val _estado = MutableStateFlow<CursoFormState>(CursoFormState.Inactivo)
    val estado: StateFlow<CursoFormState> = _estado.asStateFlow()

    fun guardar(nombre: String, descripcion: String) {
        if (nombre.isBlank() || descripcion.isBlank()) {
            _estado.value = CursoFormState.Error(MENSAJE_CAMPOS_VACIOS)
            return
        }

        viewModelScope.launch {
            _estado.value = CursoFormState.Guardando
            try {
                // withTimeout evita que el spinner se quede pegado para siempre si el Task de
                // Firestore nunca se completa (p. ej. reintentos internos por un error de permisos).
                withTimeout(TIMEOUT_MS) {
                    if (esEdicion) {
                        repository.actualizarCurso(cursoId, nombre, descripcion)
                    } else {
                        repository.crearCurso(nombre, descripcion)
                    }
                }
                _estado.value = CursoFormState.Guardado
            } catch (e: TimeoutCancellationException) {
                _estado.value = CursoFormState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = CursoFormState.Error(e.message ?: "Error al guardar el curso")
            }
        }
    }

    private companion object {
        const val MENSAJE_CAMPOS_VACIOS = "Completa el nombre y la descripción"
        const val TIMEOUT_MS = 15_000L
        const val MENSAJE_TIMEOUT = "No se pudo conectar con Firestore (tiempo de espera agotado). " +
            "Revisa tu conexión o que Firestore esté habilitado en Firebase."
    }
}
