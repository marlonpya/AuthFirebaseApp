package com.microsol.authfirebaseapp.presentation.notificaciones

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.FragmentNotificacionesBinding
import com.microsol.authfirebaseapp.domain.model.Notificacion
import kotlinx.coroutines.launch

/**
 * Pantalla que lista las notificaciones recibidas por Firebase Cloud Messaging y guardadas
 * localmente con Room. El usuario solo puede LEER (tocar una notificación la marca como leída) y
 * ELIMINAR (icono de basurero). No se crean ni editan notificaciones desde la app: llegan por FCM y
 * las persiste AppMessagingService.onMessageReceived.
 *
 * Igual que Cursos/Tareas usa MVVM: toda la lógica de datos vive en NotificacionesViewModel; este
 * Fragment solo dibuja lo que el ViewModel expone vía StateFlow y pide el permiso de notificaciones.
 */
class NotificacionesFragment : Fragment() {

    private var _binding: FragmentNotificacionesBinding? = null
    private val binding get() = _binding!!

    // Necesita el Context para construir Room, por eso la factory recibe requireContext().
    private val viewModel: NotificacionesViewModel by viewModels {
        NotificacionesViewModelFactory(requireContext())
    }

    private lateinit var adapter: NotificacionesAdapter

    // Lanzador del permiso runtime POST_NOTIFICATIONS (obligatorio desde Android 13 / API 33).
    // Si el usuario lo niega, la app igual guarda las notificaciones en Room, pero no podrá
    // mostrarlas en la barra de estado del sistema.
    private val permisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            Snackbar.make(
                binding.root,
                R.string.notificaciones_permiso_denegado,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pedirPermisoNotificacionesSiHaceFalta()
        setupRecyclerView()
        observarEstado()
        // Nota: aquí NO se llama a un cargar(): el ViewModel ya se suscribe al Flow de Room en su init.
    }

    /**
     * En Android 13+ el permiso POST_NOTIFICATIONS es runtime: sin él, el sistema no muestra las
     * notificaciones (aunque no lance error). En versiones anteriores no existe y no hay que pedirlo.
     */
    private fun pedirPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val yaConcedido = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!yaConcedido) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificacionesAdapter(
            onClick = { notificacion -> viewModel.marcarComoLeida(notificacion) },
            onEliminar = { notificacion -> confirmarEliminar(notificacion) }
        )
        binding.recyclerNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotificaciones.adapter = adapter
    }

    private fun confirmarEliminar(notificacion: Notificacion) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.notificaciones_eliminar_titulo)
            .setMessage(R.string.notificaciones_eliminar_mensaje)
            .setPositiveButton(R.string.notificaciones_eliminar_confirmar) { _, _ ->
                viewModel.eliminar(notificacion)
            }
            .setNegativeButton(R.string.notificaciones_eliminar_cancelar, null)
            .show()
    }

    /** Colecta el StateFlow del ViewModel respetando el ciclo de vida de la vista. */
    private fun observarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estado.collect { estado ->
                    when (estado) {
                        is NotificacionesState.Loading -> mostrarCargando()
                        is NotificacionesState.Exito -> mostrarNotificaciones(estado.notificaciones)
                        is NotificacionesState.Error -> mostrarError(estado.mensaje)
                    }
                }
            }
        }
    }

    private fun mostrarCargando() {
        binding.progressBar.visibility = View.VISIBLE
        binding.textNotificacionesVacio.visibility = View.GONE
        binding.recyclerNotificaciones.visibility = View.GONE
    }

    private fun mostrarNotificaciones(notificaciones: List<Notificacion>) {
        binding.progressBar.visibility = View.GONE
        if (notificaciones.isEmpty()) {
            binding.recyclerNotificaciones.visibility = View.GONE
            binding.textNotificacionesVacio.visibility = View.VISIBLE
        } else {
            binding.textNotificacionesVacio.visibility = View.GONE
            binding.recyclerNotificaciones.visibility = View.VISIBLE
            adapter.submitList(notificaciones)
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
