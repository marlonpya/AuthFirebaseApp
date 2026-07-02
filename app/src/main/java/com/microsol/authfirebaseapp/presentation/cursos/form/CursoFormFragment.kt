package com.microsol.authfirebaseapp.presentation.cursos.form

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
import com.microsol.authfirebaseapp.databinding.FragmentCursoFormBinding
import kotlinx.coroutines.launch

/**
 * Formulario de crear/editar un curso. Si [args].cursoId llega vacío es modo creación; si no,
 * modo edición (los campos se prellenan con lo que ya trae CursosAdapter, sin otra lectura a
 * Firestore). Al guardar exitosamente, vuelve a CursosFragment con popBackStack().
 */
class CursoFormFragment : Fragment() {

    private var _binding: FragmentCursoFormBinding? = null
    private val binding get() = _binding!!

    private val args: CursoFormFragmentArgs by navArgs()

    private val viewModel: CursoFormViewModel by viewModels {
        CursoFormViewModelFactory(args.cursoId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCursoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textTituloFormulario.text = if (viewModel.esEdicion) {
            getString(R.string.curso_form_titulo_editar)
        } else {
            getString(R.string.curso_form_titulo_nuevo)
        }
        binding.editNombre.setText(args.nombre)
        binding.editDescripcion.setText(args.descripcion)

        binding.botonGuardar.setOnClickListener {
            val nombre = binding.editNombre.text?.toString()?.trim().orEmpty()
            val descripcion = binding.editDescripcion.text?.toString()?.trim().orEmpty()
            viewModel.guardar(nombre, descripcion)
        }

        observarEstado()
    }

    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    when (estado) {
                        is CursoFormState.Inactivo -> Unit
                        is CursoFormState.Guardando -> mostrarCargando(true)
                        is CursoFormState.Guardado -> {
                            mostrarCargando(false)
                            findNavController().popBackStack()
                        }
                        is CursoFormState.Error -> {
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
}
