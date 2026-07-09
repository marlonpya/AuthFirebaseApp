package com.microsol.authfirebaseapp.presentation.tareas.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
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
 * creación; si no, modo edición (título/fecha/fotos se prellenan con lo que ya trae
 * TareasAdapter, sin otra lectura a Firestore). Hasta 3 fotos por tarea, elegidas con el Photo
 * Picker del sistema: se muestran como vista previa local y solo se suben a Storage al tocar
 * "Guardar" (ver TareaFormViewModel.guardar). Al guardar exitosamente, vuelve a TareasFragment
 * con popBackStack().
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

    // Estado local de las 3 casillas de foto: no se sube nada hasta que se toca "Guardar".
    private val slots = MutableList<FotoSlot>(NUMERO_CASILLAS) { FotoSlot.Vacio }
    private val urlsOriginales: List<String> by lazy { args.imagenesUrls?.toList() ?: emptyList() }
    private lateinit var vistasFoto: List<Triple<ImageView, ImageButton, ActivityResultLauncher<PickVisualMediaRequest>>>

    // Deben registrarse como inicializadores de campo (no dentro de onViewCreated): Activity
    // Result API exige registrar el launcher antes de que el Fragment llegue a STARTED.
    private val selectorFoto1 = registrarSelectorFoto(0)
    private val selectorFoto2 = registrarSelectorFoto(1)
    private val selectorFoto3 = registrarSelectorFoto(2)

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

        configurarCasillasFoto()

        binding.botonGuardar.setOnClickListener {
            val titulo = binding.editTitulo.text?.toString()?.trim().orEmpty()
            viewModel.guardar(titulo, fechaLimiteSeleccionada, slots.toList(), urlsOriginales)
        }

        observarEstado()
    }

    private fun configurarCasillasFoto() {
        vistasFoto = listOf(
            Triple(binding.imageFoto1, binding.botonQuitarFoto1, selectorFoto1),
            Triple(binding.imageFoto2, binding.botonQuitarFoto2, selectorFoto2),
            Triple(binding.imageFoto3, binding.botonQuitarFoto3, selectorFoto3)
        )
        vistasFoto.forEachIndexed { indice, (imagen, botonQuitar, selector) ->
            urlsOriginales.getOrNull(indice)?.let { url -> slots[indice] = FotoSlot.Existente(url) }
            imagen.setOnClickListener {
                selector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            botonQuitar.setOnClickListener {
                slots[indice] = FotoSlot.Vacio
                actualizarVistaCasilla(indice)
            }
            actualizarVistaCasilla(indice)
        }
    }

    private fun registrarSelectorFoto(indice: Int): ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val tipoContenido = requireContext().contentResolver.getType(uri)
                slots[indice] = FotoSlot.Nueva(uri.toString(), tipoContenido)
                actualizarVistaCasilla(indice)
            }
        }

    private fun actualizarVistaCasilla(indice: Int) {
        val (imagen, botonQuitar, _) = vistasFoto[indice]
        when (val slot = slots[indice]) {
            FotoSlot.Vacio -> {
                imagen.setImageResource(android.R.drawable.ic_menu_gallery)
                botonQuitar.visibility = View.GONE
            }
            is FotoSlot.Existente -> {
                imagen.load(slot.url)
                botonQuitar.visibility = View.VISIBLE
            }
            is FotoSlot.Nueva -> {
                imagen.load(slot.uriLocal)
                botonQuitar.visibility = View.VISIBLE
            }
        }
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
        const val NUMERO_CASILLAS = 3
    }
}
