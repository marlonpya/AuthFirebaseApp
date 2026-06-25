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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarDatosUsuario()
        binding.botonCerrarSesion.setOnClickListener { cerrarSesion() }
    }

    /** Lee el usuario actual de Firebase y pinta su nombre, correo y foto en pantalla. */
    private fun mostrarDatosUsuario() {
        val usuario = auth.currentUser ?: return

        binding.textNombre.text = usuario.displayName ?: getString(R.string.msg_sin_nombre)
        binding.textCorreo.text = usuario.email ?: getString(R.string.msg_sin_correo)

        // La foto de perfil es una URL remota (photoUrl). Un ImageView no descarga imágenes por sí solo,
        // por eso usamos una librería de imágenes (Coil) que las descarga y cachea con una sola línea.
        usuario.photoUrl?.let { url ->
            binding.imageFoto.load(url)
        }
    }

    /**
     * Cierra la sesión por completo:
     *  1. FirebaseAuth.signOut(): elimina la sesión de Firebase.
     *  2. clearCredentialState(): limpia el estado de Credential Manager (recomendado por Google) para
     *     que la próxima vez el usuario pueda elegir cuenta de nuevo en lugar de reusar la anterior.
     *  3. Vuelve a la pantalla de login limpiando el back stack.
     */
    private fun cerrarSesion() {
        auth.signOut()

        val credentialManager = CredentialManager.create(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
                // Si la limpieza falla no es crítico: la sesión de Firebase ya se cerró.
            }
            irALogin()
        }
    }

    /** Navega de vuelta a Login usando la acción de Safe Args (limpia el back stack). */
    private fun irALogin() {
        val accion = HomeFragmentDirections.actionHomeToLogin()
        findNavController().navigate(accion)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
