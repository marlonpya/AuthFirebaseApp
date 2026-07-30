package com.microsol.authfirebaseapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos Room de la app. Por ahora solo tiene la tabla de notificaciones (feature de
 * Notificaciones); Cursos/Tareas siguen viviendo en Firestore (nube), no aquí.
 *
 * Se expone como singleton con [getInstance] porque el proyecto NO usa un framework de inyección
 * de dependencias (ni Hilt ni Koin): tanto el NotificacionesViewModelFactory como
 * AppMessagingService necesitan el mismo DAO, y crear varias instancias de RoomDatabase apuntando
 * al mismo archivo daría problemas. Guardamos el applicationContext para no filtrar una Activity.
 */
@Database(entities = [NotificacionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificacionDao(): NotificacionDao

    companion object {
        private const val NOMBRE_BD = "authfirebaseapp.db"

        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // Doble comprobación (double-checked locking) para que sea seguro entre hilos: el
            // servicio de mensajería y la UI pueden pedir la instancia desde hilos distintos.
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                ).build().also { instancia = it }
            }
        }
    }
}
