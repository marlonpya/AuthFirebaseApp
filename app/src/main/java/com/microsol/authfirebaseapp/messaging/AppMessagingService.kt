package com.microsol.authfirebaseapp.messaging

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.microsol.authfirebaseapp.MainActivity
import com.microsol.authfirebaseapp.R
import com.microsol.authfirebaseapp.data.local.AppDatabase
import com.microsol.authfirebaseapp.data.repository.RoomNotificacionRepositoryImpl
import com.microsol.authfirebaseapp.domain.repository.NotificacionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de Firebase Cloud Messaging. El sistema lo instancia cuando llega un mensaje o cuando se
 * renueva el token; se registra en el AndroidManifest con el intent-filter
 * "com.google.firebase.MESSAGING_EVENT".
 *
 * Cada notificación recibida se GUARDA en Room (para que aparezca en NotificacionesFragment) y se
 * MUESTRA en la barra de estado. Recordatorio de comportamiento de FCM:
 *  - App en primer plano: siempre pasa por [onMessageReceived] (mensajes notification y data).
 *  - App en segundo plano/cerrada: los mensajes de tipo "notification" los muestra el sistema y NO
 *    pasan por aquí; por eso, para poder persistirlos en Room, hay que enviarlos como mensajes
 *    "data" (solo payload data), que siempre disparan [onMessageReceived]. Ver el prompt/README.
 */
class AppMessagingService : FirebaseMessagingService() {

    // Scope propio del servicio: la inserción en Room es una operación suspend y debe ir fuera del
    // hilo principal. SupervisorJob evita que un fallo en una inserción cancele las siguientes.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val repository: NotificacionRepository by lazy {
        RoomNotificacionRepositoryImpl(AppDatabase.getInstance(applicationContext).notificacionDao())
    }

    /**
     * Se dispara cuando FCM genera o renueva el token de registro del dispositivo. En esta lección
     * no hay backend propio al que reenviarlo, así que basta con loguearlo (se puede copiar desde
     * Logcat para enviarse una notificación de prueba desde la consola de Firebase).
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Nuevo token FCM: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // El título/cuerpo pueden venir en el bloque "notification" (compositor de Firebase) o en el
        // bloque "data" (mensajes data-only, los que se persisten también en segundo plano).
        val titulo = message.notification?.title
            ?: message.data[CLAVE_TITULO]
            ?: getString(R.string.notificaciones_titulo_default)
        val cuerpo = message.notification?.body
            ?: message.data[CLAVE_CUERPO]
            ?: ""

        // 1. Persistir en Room para que se vea en la pantalla de Notificaciones.
        scope.launch {
            repository.guardar(titulo, cuerpo, System.currentTimeMillis())
        }

        // 2. Mostrarla en la barra de estado (los mensajes data-only NO se muestran solos).
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        // Al tocar la notificación se abre la app (MainActivity). Se deja simple: no hace deep-link
        // directo a la pantalla de Notificaciones.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true) // Se cierra al tocarla.
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        // notify() no lanza excepción si falta el permiso POST_NOTIFICATIONS (Android 13+): sencillamente
        // no se muestra. La notificación igual quedó guardada en Room en el paso 1.
        val manager = NotificationManagerCompat.from(this)
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        manager.notify(id, notificacion)
    }

    companion object {
        private const val TAG = "AppMessagingService"

        /** Id del canal de notificaciones. Debe coincidir con el que crea MainActivity.onCreate. */
        const val CANAL_ID = "canal_notificaciones"

        // Claves esperadas en el payload "data" de un mensaje data-only.
        private const val CLAVE_TITULO = "titulo"
        private const val CLAVE_CUERPO = "cuerpo"
    }
}
