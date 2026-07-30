package com.microsol.authfirebaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.microsol.authfirebaseapp.messaging.AppMessagingService

/**
 * Única Activity de la app. Actúa como "host" del Navigation Component:
 * solo carga el layout que contiene el NavHostFragment y deja que el
 * nav_graph decida qué pantalla mostrar (Login al inicio, Home tras autenticarse).
 *
 * Aquí NO va lógica de autenticación: esa lógica vive directamente en los Fragments
 * (LoginFragment y HomeFragment) para que sea fácil de leer y explicar en clase.
 *
 * Sí crea aquí el canal de notificaciones de Firebase Cloud Messaging: desde Android 8 (API 26)
 * toda notificación debe pertenecer a un NotificationChannel, y basta crearlo una sola vez al
 * arrancar (crearlo de nuevo con el mismo id no tiene efecto).
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        crearCanalNotificaciones()
    }

    /**
     * Crea el canal por el que se muestran las notificaciones (feature de Notificaciones).
     * Solo aplica en Android 8+; en versiones anteriores los canales no existen y no hace falta.
     */
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val canal = NotificationChannel(
            AppMessagingService.CANAL_ID,
            getString(R.string.notificaciones_canal_nombre),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notificaciones_canal_descripcion)
        }

        // getSystemService (extensión de androidx.core) devuelve el NotificationManager tipado.
        getSystemService<NotificationManager>()?.createNotificationChannel(canal)
    }
}
