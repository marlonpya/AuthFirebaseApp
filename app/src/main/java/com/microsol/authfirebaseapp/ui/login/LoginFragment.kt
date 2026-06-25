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

    override fun onDestroyView() {
        super.onDestroyView()
        // Evitamos fugas de memoria liberando la referencia al binding.
        _binding = null
    }
}
