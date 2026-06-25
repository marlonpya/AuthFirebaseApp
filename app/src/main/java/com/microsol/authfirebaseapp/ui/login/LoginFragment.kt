package com.microsol.authfirebaseapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión.
 *
 * Permite autenticarse de dos formas con Firebase Authentication:
 *  1. Correo y contraseña (signInWithEmailAndPassword / createUserWithEmailAndPassword).
 *  2. Cuenta de Google, usando la API vigente **Credential Manager** + "Sign in with Google".
 *     (NO se usa el obsoleto GoogleSignInClient / onActivityResult de play-services-auth).
 *
 * Toda la lógica vive aquí, en el Fragment, de forma lineal y comentada (sin ViewModel ni Repository).
 */
class LoginFragment : Fragment() {

    // Patrón de ViewBinding recomendado: _binding solo es válido entre onCreateView y onDestroyView.
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Instancia única de Firebase Authentication.
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si ya hay una sesión activa, saltamos directo a Home (no mostramos el login de nuevo).
        if (auth.currentUser != null) {
            irAHome()
            return
        }

        // Conectamos los botones con sus acciones.
        binding.botonIngresar.setOnClickListener { ingresarConCorreo() }
        binding.botonRegistrarse.setOnClickListener { registrarConCorreo() }
        binding.botonGoogle.setOnClickListener { iniciarSesionConGoogle() }
    }

    // ----------------------------------------------------------------------------------
    // Autenticación con correo y contraseña
    // ----------------------------------------------------------------------------------

    /** Inicia sesión con un correo y contraseña ya registrados. */
    private fun ingresarConCorreo() {
        val correo = binding.editCorreo.text?.toString()?.trim().orEmpty()
        val password = binding.editPassword.text?.toString().orEmpty()
        if (correo.isEmpty() || password.isEmpty()) {
            mostrarMensaje(getString(R.string.msg_completa_campos))
            return
        }

        mostrarCargando(true)
        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener { tarea ->
                mostrarCargando(false)
                if (tarea.isSuccessful) {
                    mostrarMensaje(getString(R.string.msg_login_correcto))
                    irAHome()
                } else {
                    mostrarMensaje(tarea.exception?.localizedMessage ?: getString(R.string.msg_error_generico))
                }
            }
    }

    /** Crea una cuenta nueva con correo y contraseña. */
    private fun registrarConCorreo() {
        val correo = binding.editCorreo.text?.toString()?.trim().orEmpty()
        val password = binding.editPassword.text?.toString().orEmpty()
        if (correo.isEmpty() || password.isEmpty()) {
            mostrarMensaje(getString(R.string.msg_completa_campos))
            return
        }

        mostrarCargando(true)
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { tarea ->
                mostrarCargando(false)
                if (tarea.isSuccessful) {
                    mostrarMensaje(getString(R.string.msg_registro_correcto))
                    irAHome()
                } else {
                    mostrarMensaje(tarea.exception?.localizedMessage ?: getString(R.string.msg_error_generico))
                }
            }
    }

    // ----------------------------------------------------------------------------------
    // Autenticación con Google (Credential Manager)
    // ----------------------------------------------------------------------------------

    /**
     * Inicia el flujo de "Sign in with Google" con Credential Manager.
     *
     * Pasos:
     *  1. Configuramos GetGoogleIdOption con el **Web Client ID** (R.string.default_web_client_id,
     *     generado por el plugin google-services a partir de google-services.json).
     *  2. Creamos la petición de credencial.
     *  3. Pedimos la credencial al CredentialManager (es una operación suspend -> corrutina).
     *  4. Si el usuario elige una cuenta, recibimos un GoogleIdTokenCredential con el idToken.
     */
    private fun iniciarSesionConGoogle() {
        // El idToken de Google debe emitirse para nuestro "servidor": el Web Client ID, NO el de Android.
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            // false = permite elegir cualquier cuenta de Google del dispositivo (no solo las ya usadas).
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(requireContext())

        mostrarCargando(true)
        // getCredential es suspend: lo ejecutamos en una corrutina ligada al ciclo de vida de la vista.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resultado = credentialManager.getCredential(requireContext(), request)
                val credencial = resultado.credential

                // Verificamos que sea una credencial de tipo "Google ID Token".
                if (credencial is CustomCredential &&
                    credencial.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredencial = GoogleIdTokenCredential.createFrom(credencial.data)
                    autenticarConFirebase(googleCredencial.idToken)
                } else {
                    mostrarCargando(false)
                    mostrarMensaje(getString(R.string.msg_error_generico))
                }
            } catch (e: GetCredentialException) {
                // Se entra aquí si el usuario cancela o si no hay cuentas disponibles.
                mostrarCargando(false)
                mostrarMensaje(getString(R.string.msg_google_cancelado))
            }
        }
    }

    /**
     * Intercambia el idToken de Google por una credencial de Firebase y termina el inicio de sesión.
     */
    private fun autenticarConFirebase(idToken: String) {
        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credencialFirebase)
            .addOnCompleteListener { tarea ->
                mostrarCargando(false)
                if (tarea.isSuccessful) {
                    mostrarMensaje(getString(R.string.msg_login_correcto))
                    irAHome()
                } else {
                    mostrarMensaje(tarea.exception?.localizedMessage ?: getString(R.string.msg_error_generico))
                }
            }
    }

    // ----------------------------------------------------------------------------------
    // Utilidades
    // ----------------------------------------------------------------------------------

    /** Navega a Home usando la acción de Safe Args (que limpia el back stack del login). */
    private fun irAHome() {
        val accion = LoginFragmentDirections.actionLoginToHome()
        findNavController().navigate(accion)
    }

    /** Muestra u oculta el ProgressBar y deshabilita los botones mientras se autentica. */
    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.botonIngresar.isEnabled = !cargando
        binding.botonRegistrarse.isEnabled = !cargando
        binding.botonGoogle.isEnabled = !cargando
    }

    private fun mostrarMensaje(texto: String) {
        Snackbar.make(binding.root, texto, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Evitamos fugas de memoria liberando la referencia al binding.
        _binding = null
    }
}
