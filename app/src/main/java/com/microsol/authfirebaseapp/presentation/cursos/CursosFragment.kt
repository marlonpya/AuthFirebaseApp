package com.microsol.authfirebaseapp.presentation.cursos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.FragmentCursosBinding
import com.microsol.authfirebaseapp.domain.model.Curso
import kotlinx.coroutines.launch

/**
 * Pantalla que lista el catálogo global de cursos (Firestore, colección "cursos") y permite
 * crear, editar y eliminar cursos (único CRUD completo del proyecto; Tareas sigue siendo solo
 * lectura + toggle de completada). Al tocar la tarjeta de un curso navega a TareasFragment.
 *
 * A diferencia de Login/Home, esta pantalla usa MVVM: toda la lógica de datos vive en
 * CursosViewModel; este Fragment solo dibuja lo que el ViewModel expone vía StateFlow.
 */
class CursosFragment : Fragment() {

    private var _binding: FragmentCursosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CursosViewModel by viewModels { CursosViewModelFactory() }

    private lateinit var adapter: CursosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCursosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observarEstado()
        // Se llama aquí (no en el init del ViewModel) porque onViewCreated() se vuelve a ejecutar
        // al volver del formulario de crear/editar, y así la lista queda siempre actualizada.
        viewModel.cargarCursos()
    }

    private fun setupRecyclerView() {
        adapter = CursosAdapter(
            onClick = { curso ->
                val accion = CursosFragmentDirections.actionCursosToTareas(curso.id)
                findNavController().navigate(accion)
            },
            onEditar = { curso ->
                val accion = CursosFragmentDirections.actionCursosToForm(
                    cursoId = curso.id,
                    nombre = curso.nombre,
                    descripcion = curso.descripcion
                )
                findNavController().navigate(accion)
            },
            onEliminar = { curso -> confirmarEliminar(curso) }
        )
        binding.recyclerCursos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCursos.adapter = adapter
    }

    private fun setupFab() {
        binding.fabNuevoCurso.setOnClickListener {
            findNavController().navigate(CursosFragmentDirections.actionCursosToForm())
        }
    }

    private fun confirmarEliminar(curso: Curso) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cursos_eliminar_titulo)
            .setMessage(getString(R.string.cursos_eliminar_mensaje, curso.nombre))
            .setPositiveButton(R.string.cursos_eliminar_confirmar) { _, _ -> viewModel.eliminarCurso(curso) }
            .setNegativeButton(R.string.cursos_eliminar_cancelar, null)
            .show()
    }

    /** Colecta el StateFlow del ViewModel respetando el ciclo de vida de la vista. */
    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    when (estado) {
                        is CursosState.Loading -> mostrarCargando()
                        is CursosState.Exito -> mostrarCursos(estado.cursos)
                        is CursosState.Error -> mostrarError(estado.mensaje)
                    }
                }
            }
        }
    }

    private fun mostrarCargando() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textCursosVacio.visibility = View.GONE
        binding.recyclerCursos.visibility = View.GONE
    }

    private fun mostrarCursos(cursos: List<Curso>) {
        binding.progressBar.visibility = View.GONE
        if (cursos.isEmpty()) {
            binding.recyclerCursos.visibility = View.GONE
            binding.textCursosVacio.visibility = View.VISIBLE
        } else {
            binding.textCursosVacio.visibility = View.GONE
            binding.recyclerCursos.visibility = View.VISIBLE
            adapter.submitList(cursos)
        }
    }

    private fun mostrarError(mensaje: String) {
        binding.progressBar.visibility = View.GONE
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
