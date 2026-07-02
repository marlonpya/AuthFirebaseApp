package com.microsol.authfirebaseapp.data

/**
 * Nombres de las colecciones raíz de Firestore, compartidos entre los distintos repositorios
 * de data/repository. Evita duplicar los strings literales, sobre todo donde un repositorio
 * necesita tocar la colección de otro (p. ej. el borrado en cascada de tareas al eliminar un curso).
 */
internal object FirestoreColecciones {
    const val CURSOS = "cursos"
    const val TAREAS = "tareas"
}
