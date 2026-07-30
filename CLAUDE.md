# AuthFirebaseApp

App Android educativa en Kotlin (proyecto de curso, carpeta `LOGRO 4/TEMA 11`). Package base:
`com.microsol.authfirebaseapp`. Usa Firebase Authentication (correo/contraseña + Google vía
Credential Manager) y Firebase Cloud Firestore (feature de Cursos/Tareas).

## Stack técnico y versiones clave

Ver `gradle/libs.versions.toml` y `app/build.gradle.kts` para la lista completa. Lo relevante:

- Kotlin + AGP `9.2.1`, `compileSdk 37`, `minSdk 24`, `targetSdk 36`.
- Navigation Component `2.10.0-alpha05` — **no bajar a la 2.9.x estable**: falla con
  "safeargs plugin must be used with android plugin" por incompatibilidad con AGP 9.x.
- Firebase BoM `34.4.0` (fija las versiones de `firebase-auth`, `firebase-firestore`,
  `firebase-storage` y `firebase-messaging`).
- Credential Manager `1.5.0` + `googleid` (API vigente de "Sign in with Google"; **no** se usa el
  `GoogleSignInClient` obsoleto).
- Coroutines `1.10.1`, Coil `2.7.0` (carga de imágenes), ViewBinding habilitado
  (`buildFeatures.viewBinding = true`), sin Compose.
- Room `2.8.4` (persistencia local del feature de Notificaciones) procesado con **KSP**. Room es la
  **única** base de datos local; Cursos/Tareas siguen en Firestore (nube).
- **Kotlin: se usa el "integrado" de AGP 9** (KGP `2.2.10`), NO se declara un plugin
  `org.jetbrains.kotlin.android` aparte (aplicarlo falla en AGP 9 con `ApplicationExtensionImpl ...
  cannot be cast to ... BaseExtension`, porque AGP 9 solo usa el DSL nuevo). Por eso **KSP tiene que
  ser la versión que casa con ese Kotlin integrado: `2.2.10-2.0.2`** (`ksp` en `libs.versions.toml`).
  No subir KSP a `2.2.20-x` (para Kotlin 2.2.20): fallaría con "KSP is not compatible with Android
  Gradle Plugin's built-in Kotlin". Si algún día se sube AGP y cambia su Kotlin integrado, alinear
  `ksp` a la versión correspondiente. **No** hace falta `android.builtInKotlin=false` ni fijar
  `jvmTarget` a mano: el Kotlin integrado ya lo alinea con `compileOptions` (Java 11). Sí es
  necesario **`android.disallowKotlinSourceSets=false`** en `gradle.properties`: KSP registra sus
  fuentes generadas con `kotlin.sourceSets`, que el Kotlin integrado bloquea por defecto; este flag
  lo permite (lo indica el propio AGP en el error "Using kotlin.sourceSets DSL ... not allowed").
- Sin DI framework (no hay Hilt/Koin): las factories de ViewModel se escriben a mano.

## Cómo compilar y correr

```
./gradlew assembleDebug
```

**Nota si se ejecuta desde WSL/Linux**: este repo vive en `/mnt/c/...` (Windows montado). El SDK
de Android configurado en `local.properties` apunta a
`C:\Users\pc\AppData\Local\Android\Sdk`, pero esa instalación solo tiene binarios `.exe` de
Windows (`aapt.exe`, `aapt2.exe`, etc.) — **no se puede compilar Gradle desde WSL/Linux** con ese
SDK (falla con "Installed Build Tools revision ... is corrupted", que en realidad es un binario
Windows siendo invocado desde Linux). Además WSL no trae JDK en el PATH por defecto. Para compilar
o correr la app, hacerlo desde **Android Studio en Windows** (o `gradlew.bat` en cmd/PowerShell).

## Arquitectura: dos paradigmas conviven a propósito

Este repo mezcla deliberadamente dos estilos, cada uno correspondiente a una lección distinta del
curso. **No hay que unificarlos.**

### 1. Login / Home — planos (lección de Firebase Auth básico)

- `ui/login/LoginFragment.kt`, `ui/home/HomeFragment.kt`.
- Toda la lógica vive directo en el Fragment: sin ViewModel, sin Repository, sin capas.
- `FirebaseAuth.getInstance()` obtenido con `by lazy`.
- Llamadas a Firebase con `.addOnCompleteListener` (no `.await()`).
- Coroutines solo para las llamadas `suspend` de Credential Manager
  (`viewLifecycleOwner.lifecycleScope.launch`).
- ViewBinding con el patrón `_binding` (nullable) / `binding` (getter) / limpiado en
  `onDestroyView()`.
- Errores mostrados con `Snackbar`, carga con `ProgressBar` (visibility GONE/VISIBLE).
- Navegación con Navigation Component + Safe Args (`LoginFragmentDirections`,
  `HomeFragmentDirections`), definida en `res/navigation/nav_graph.xml`.

### 2. Cursos / Tareas — en capas, MVVM + Repository (lección de Firestore + arquitectura)

- `domain/`, `data/`, `presentation/cursos/`, `presentation/tareas/`.
- `domain/model/{Curso,Tarea}.kt` y `domain/repository/{CursoRepository,TareaRepository}.kt`:
  **cero imports de Android o Firebase** (interfaces + tipos Kotlin puros; fechas son `Long`
  epoch millis, no `Timestamp`).
- `data/model/{CursoDto,TareaDto}.kt` (anotados con `@DocumentId` para Firestore),
  `data/mapper/{CursoMapper,TareaMapper}.kt` (traducen DTO ↔ dominio, incluyendo
  `Timestamp -> Long`), `data/repository/Firestore{Curso,Tarea}RepositoryImpl.kt` (implementan las
  interfaces de `domain/` usando `FirebaseFirestore` + `kotlinx-coroutines-play-services` para
  `.await()`).
- `presentation/<feature>/`: `<Feature>ViewModel` (extiende `ViewModel()` plano, no
  `AndroidViewModel`; recibe el repositorio por constructor con valor por defecto a la
  implementación real; expone `StateFlow<XState>`), `<Feature>State` (sealed class
  `Loading`/`Exito`/`Error`; el estado "vacío" se maneja revisando `.isEmpty()` en la vista),
  `<Feature>ViewModelFactory` (factory manual, no hay DI), `<Feature>Fragment`,
  `<Feature>Adapter` (`ListAdapter` + `DiffUtil`).
- El Fragment colecta el StateFlow con
  `viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(STARTED) { ... } }`.

Si se agregan más pantallas de este feature, seguir este mismo patrón de capas. Si se toca
Login/Home, **no** convertirlas a MVVM — es intencional que sigan planas.

## Árbol de paquetes

```
com.microsol.authfirebaseapp/
├── MainActivity.kt
├── ui/
│   ├── login/LoginFragment.kt
│   └── home/HomeFragment.kt
├── messaging/
│   └── AppMessagingService.kt          (FirebaseMessagingService: onNewToken + onMessageReceived)
├── domain/
│   ├── model/{Curso.kt, Tarea.kt, Notificacion.kt}
│   └── repository/{CursoRepository.kt, TareaRepository.kt, NotificacionRepository.kt}  (interfaces)
├── data/
│   ├── model/{CursoDto.kt, TareaDto.kt}
│   ├── local/{AppDatabase.kt, NotificacionDao.kt, NotificacionEntity.kt}   (Room)
│   ├── mapper/{CursoMapper.kt, TareaMapper.kt, NotificacionMapper.kt}
│   └── repository/{FirestoreCursoRepositoryImpl.kt, FirestoreTareaRepositoryImpl.kt, RoomNotificacionRepositoryImpl.kt}
└── presentation/
    ├── cursos/{CursosViewModel, CursosState, CursosViewModelFactory, CursosFragment, CursosAdapter}
    ├── tareas/{TareasViewModel, TareasState, TareasViewModelFactory, TareasFragment, TareasAdapter}
    └── notificaciones/{NotificacionesViewModel, NotificacionesState, NotificacionesViewModelFactory, NotificacionesFragment, NotificacionesAdapter}
```

### 3. Notificaciones — en capas (MVVM + Repository) pero con Room local + FCM

Sigue el **mismo patrón en capas que Cursos/Tareas** (no es un tercer paradigma), con dos diferencias:

- La fuente de datos es **local con Room** (`data/local/`), no Firestore. `NotificacionRepository`
  expone `observarNotificaciones(): Flow<List<Notificacion>>` (reactivo: Room reemite la lista al
  insertar/leer/eliminar), y `NotificacionesViewModel` se suscribe en su `init` (no hay un
  `cargar()` que llame el Fragment). `NotificacionesViewModelFactory` **necesita un `Context`** para
  construir la BD, a diferencia de las otras factories.
- Las notificaciones llegan por **Firebase Cloud Messaging**: `messaging/AppMessagingService`
  (`onMessageReceived`) las persiste en Room y las muestra en la barra. Para que se guarden también
  con la app en segundo plano se usan mensajes **data-only** (título/cuerpo en el payload `data`),
  que siempre disparan `onMessageReceived`. `MainActivity.onCreate` crea el `NotificationChannel`
  (API 26+) y `NotificacionesFragment` pide el permiso runtime `POST_NOTIFICATIONS` (API 33+).
- Alcance de escritura: solo **marcar como leída** (toggle `leida`) y **eliminar**. No se crean ni
  editan notificaciones desde la app.

## Modelo de datos en Firestore

Colecciones raíz (planas, con FK — **no** subcolecciones):

- `cursos`: `id`, `nombre`, `descripcion`, `fechaCreacion`. Catálogo **global**: no hay campo de
  propietario, cualquier usuario autenticado ve todos los cursos.
- `tareas`: `id`, `cursoId` (FK a `cursos.id`), `titulo`, `completada`, `fechaLimite`. Se filtran
  con `whereEqualTo("cursoId", cursoId)`.

Reglas de seguridad en `firestore.rules` (raíz del repo, no se despliegan automáticamente —
copiar a la consola de Firebase o usar `firebase deploy --only firestore:rules`):
solo lectura/escritura si `request.auth != null` (sin restricción por dueño, porque el catálogo es
global).

Alcance de escritura actual: **solo** alternar `completada` en una tarea. No hay crear/editar/
eliminar cursos ni tareas desde la app — esos datos se cargan manualmente en la consola de
Firebase.

## Convenciones a seguir

- Comentarios y `strings.xml` **en español**, con tono didáctico/explicativo (es material de
  curso).
- IDs de vista en camelCase en español con prefijo del tipo de widget:
  `textNombre`, `botonCerrarSesion`, `imageFoto`, `editCorreo`, `progressBar`.
- Layouts: `ConstraintLayout` como raíz, `tools:context`/`tools:text` para previsualización,
  `fragment_<nombre>.xml` / `item_<nombre>.xml` como convención de nombres.
- ViewBinding en todos lados (nunca `findViewById`): patrón `_binding`/`binding`, limpiado en
  `onDestroyView()`.
- Navegación siempre con Navigation Component + Safe Args (nunca `Bundle` manual):
  `findNavController().navigate(FooFragmentDirections.actionXToY(...))`.
- Errores → `Snackbar`; carga → `ProgressBar` visible/gone (no `SwipeRefreshLayout`, no está en
  el proyecto).

## Dónde mirar antes de tocar algo

- `app/src/main/res/navigation/nav_graph.xml` — todas las pantallas y acciones de navegación.
- `gradle/libs.versions.toml` — versiones y catálogo de dependencias (usar siempre `libs.xxx`,
  nunca hardcodear coordenadas Maven en `app/build.gradle.kts`).
- `firestore.rules` — permisos de lectura/escritura de Firestore.
- `app/google-services.json` — configuración de Firebase (no versionar cambios sensibles).
