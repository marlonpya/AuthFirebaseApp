package com.microsol.authfirebaseapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

/**
 * Pantalla que se muestra cuando el usuario ya está autenticado.
 *
 * Responsabilidades:
 *  - Mostrar los datos del usuario actual de Firebase (nombre, correo y foto).
 *  - Cerrar sesión: cerrar la sesión de Firebase y limpiar el estado de credenciales.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
