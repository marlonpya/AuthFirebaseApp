package com.microsol.authfirebaseapp.presentation.tareas.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.FragmentTareaFormBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formulario de crear/editar una tarea de un curso. Si [args].tareaId llega vacío es modo
 * creación; si no, modo edición (título/fecha se prellenan con lo que ya trae TareasAdapter, sin
 * otra lectura a Firestore). Al guardar exitosamente, vuelve a TareasFragment con popBackStack().
 */
class TareaFormFragment : Fragment() {

    private var _binding: FragmentTareaFormBinding? = null
    private val binding get() = _binding!!

    private val args: TareaFormFragmentArgs by navArgs()

    private val viewModel: TareaFormViewModel by viewModels {
        TareaFormViewModelFactory(args.cursoId, args.tareaId)
    }

    private var fechaLimiteSeleccionada: Long? = null
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTareaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textTituloFormulario.text = if (viewModel.esEdicion) {
            getString(R.string.tarea_form_titulo_editar)
        } else {
            getString(R.string.tarea_form_titulo_nuevo)
        }
        binding.editTitulo.setText(args.titulo)
        if (args.fechaLimite != SIN_FECHA) {
            fechaLimiteSeleccionada = args.fechaLimite
            binding.editFechaLimite.setText(formatoFecha.format(Date(args.fechaLimite)))
        }
        binding.editFechaLimite.setOnClickListener { mostrarSelectorFecha() }
        binding.inputFechaLimite.setEndIconOnClickListener { mostrarSelectorFecha() }

        binding.botonGuardar.setOnClickListener {
            val titulo = binding.editTitulo.text?.toString()?.trim().orEmpty()
            viewModel.guardar(titulo, fechaLimiteSeleccionada)
        }

        observarEstado()
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        fechaLimiteSeleccionada?.let { calendario.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                calendario.set(anio, mes, dia, 0, 0, 0)
                fechaLimiteSeleccionada = calendario.timeInMillis
                binding.editFechaLimite.setText(formatoFecha.format(calendario.time))
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    when (estado) {
                        is TareaFormState.Inactivo -> Unit
                        is TareaFormState.Guardando -> mostrarCargando(true)
                        is TareaFormState.Guardado -> {
                            mostrarCargando(false)
                            findNavController().popBackStack()
                        }
                        is TareaFormState.Error -> {
                            mostrarCargando(false)
                            Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.botonGuardar.isEnabled = !cargando
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val SIN_FECHA = -1L
    }
}
