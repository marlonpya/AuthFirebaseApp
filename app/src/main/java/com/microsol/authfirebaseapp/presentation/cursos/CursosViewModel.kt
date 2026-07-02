package com.microsol.authfirebaseapp.presentation.cursos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsol.authfirebaseapp.data.repository.FirestoreCursoRepositoryImpl
import com.microsol.authfirebaseapp.domain.model.Curso
import com.microsol.authfirebaseapp.domain.repository.CursoRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * ViewModel de CursosFragment. No conoce Fragment/View/Context: solo expone [estado] como
 * StateFlow y recibe el repositorio por constructor (aquí con un valor por defecto a la
 * implementación real de Firestore, para no requerir un framework de inyección de dependencias).
 * Al recibir un fake CursoRepository, es instanciable y testeable en un test unitario JVM puro.
 *
 * No tiene un `init { cargarCursos() }` automático a propósito: CursosFragment llama a
 * [cargarCursos] explícitamente en onViewCreated(), que se vuelve a ejecutar cada vez que se
 * vuelve del formulario de creación/edición (Navigation Component recrea la vista pero reutiliza
 * la misma instancia de Fragment/ViewModel), así la lista se refresca sin duplicar la carga inicial.
 */
class CursosViewModel(
    private val repository: CursoRepository = FirestoreCursoRepositoryImpl()
) : ViewModel() {

    private val _estado = MutableStateFlow<CursosState>(CursosState.Loading)
    val estado: StateFlow<CursosState> = _estado.asStateFlow()

    fun cargarCursos() {
        viewModelScope.launch {
            _estado.value = CursosState.Loading
            try {
                val cursos = withTimeout(TIMEOUT_MS) { repository.obtenerCursos() }
                _estado.value = CursosState.Exito(cursos)
            } catch (e: TimeoutCancellationException) {
                _estado.value = CursosState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = CursosState.Error(e.message ?: "Error al cargar cursos")
            }
        }
    }

    /** Elimina el curso (y sus tareas, en cascada, ver FirestoreCursoRepositoryImpl) y recarga la lista. */
    fun eliminarCurso(curso: Curso) {
        viewModelScope.launch {
            try {
                withTimeout(TIMEOUT_MS) { repository.eliminarCurso(curso.id) }
                cargarCursos()
            } catch (e: TimeoutCancellationException) {
                _estado.value = CursosState.Error(MENSAJE_TIMEOUT)
            } catch (e: Exception) {
                _estado.value = CursosState.Error(e.message ?: "Error al eliminar el curso")
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
