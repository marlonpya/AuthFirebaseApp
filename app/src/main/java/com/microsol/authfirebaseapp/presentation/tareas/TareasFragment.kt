package com.microsol.authfirebaseapp.presentation.tareas

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.FragmentTareasBinding
import com.microsol.authfirebaseapp.domain.model.Tarea
import kotlinx.coroutines.launch

/**
 * Pantalla que lista las tareas de un curso (Firestore, colección "tareas" filtrada por la FK
 * cursoId, sin subcolecciones) y permite crear, editar y eliminar tareas, además de alternar
 * "completada" con el checkbox. El cursoId llega por Safe Args desde CursosFragment.
 */
class TareasFragment : Fragment() {

    private var _binding: FragmentTareasBinding? = null
    private val binding get() = _binding!!

    private val args: TareasFragmentArgs by navArgs()

    private val viewModel: TareasViewModel by viewModels { TareasViewModelFactory(args.cursoId) }

    private lateinit var adapter: TareasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTareasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observarEstado()
        // Se llama aquí (no en el init del ViewModel) porque onViewCreated() se vuelve a ejecutar
        // al volver del formulario de crear/editar, y así la lista queda siempre actualizada.
        viewModel.cargarTareas()
    }

    private fun setupRecyclerView() {
        adapter = TareasAdapter(
            onToggle = { tarea -> viewModel.alternarCompletada(tarea) },
            onEditar = { tarea ->
                val accion = TareasFragmentDirections.actionTareasToForm(
                    cursoId = args.cursoId,
                    tareaId = tarea.id,
                    titulo = tarea.titulo,
                    fechaLimite = tarea.fechaLimite
                )
                findNavController().navigate(accion)
            },
            onEliminar = { tarea -> confirmarEliminar(tarea) }
        )
        binding.recyclerTareas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTareas.adapter = adapter
    }

    private fun setupFab() {
        binding.fabNuevaTarea.setOnClickListener {
            val accion = TareasFragmentDirections.actionTareasToForm(cursoId = args.cursoId)
            findNavController().navigate(accion)
        }
    }

    private fun confirmarEliminar(tarea: Tarea) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.tareas_eliminar_titulo)
            .setMessage(getString(R.string.tareas_eliminar_mensaje, tarea.titulo))
            .setPositiveButton(R.string.tareas_eliminar_confirmar) { _, _ -> viewModel.eliminarTarea(tarea) }
            .setNegativeButton(R.string.tareas_eliminar_cancelar, null)
            .show()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    when (estado) {
                        is TareasState.Loading -> mostrarCargando()
                        is TareasState.Exito -> mostrarTareas(estado.tareas)
                        is TareasState.Error -> mostrarError(estado.mensaje)
                    }
                }
            }
        }
    }

    private fun mostrarCargando() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textTareasVacio.visibility = View.GONE
        binding.recyclerTareas.visibility = View.GONE
    }

    private fun mostrarTareas(tareas: List<Tarea>) {
        binding.progressBar.visibility = View.GONE
        if (tareas.isEmpty()) {
            binding.recyclerTareas.visibility = View.GONE
            binding.textTareasVacio.visibility = View.VISIBLE
        } else {
            binding.textTareasVacio.visibility = View.GONE
            binding.recyclerTareas.visibility = View.VISIBLE
            adapter.submitList(tareas)
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
