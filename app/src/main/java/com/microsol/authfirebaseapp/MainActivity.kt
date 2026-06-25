package com.microsol.authfirebaseapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Única Activity de la app. Actúa como "host" del Navigation Component:
 * solo carga el layout que contiene el NavHostFragment y deja que el
 * nav_graph decida qué pantalla mostrar (Login al inicio, Home tras autenticarse).
 *
 * Aquí NO va lógica de autenticación: esa lógica vive directamente en los Fragments
 * (LoginFragment y HomeFragment) para que sea fácil de leer y explicar en clase.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
