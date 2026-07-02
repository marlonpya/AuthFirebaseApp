# Prompt para Claude Code (Opus) — Proyecto de la clase: Autenticación con Firebase

> Copia y pega TODO el bloque de abajo en Claude Code (modelo Opus), dentro de una carpeta vacía donde quieras crear el proyecto.

---

Eres un experto en desarrollo Android con Kotlin. Crea un proyecto Android **didáctico** para una clase de estudiantes sobre **cómo integrar Firebase Authentication** en una app. El proyecto debe abrirse en Android Studio y compilar.

## Objetivo
Una app sencilla que permita **iniciar sesión** (con **Google** y con **correo/contraseña**), mostrar los **datos del usuario autenticado** y **cerrar sesión**. El foco es enseñar la integración de Firebase Auth, no construir una app compleja.

## Referencia de estilo
Sigue las convenciones de mi repositorio **https://github.com/marlonpya/PersistenceApp** (rama `master`): Gradle Kotlin DSL con **version catalog**, **Navigation Component + Safe Args**, **ViewBinding**, Material + ConstraintLayout, paquetes por feature, comentarios y KDoc en español.
**IMPORTANTE:** de ese repo toma SOLO el *estilo* (Gradle, navegación, ViewBinding, nombres, comentarios). **NO copies su arquitectura**: aquí NO usamos capas data/domain/presentation, ni ViewModel, ni UseCases, ni Repository, ni DI/AppContainer, ni Room.

## Restricciones técnicas (OBLIGATORIAS)
- **Kotlin** únicamente. Nada de Java.
- **UI en XML** (layouts tradicionales). **NO Jetpack Compose**.
- **Navigation Component con Fragments**: una sola Activity (`MainActivity`) con `NavHostFragment` y `nav_graph.xml`. Usa **Safe Args** para las acciones de navegación (aunque no pasemos argumentos, usa las clases `...Directions` generadas, como en el repo).
- **ViewBinding** (sin `findViewById` ni Kotlin synthetics). Usa el mismo patrón del repo:
  ```kotlin
  private var _binding: FragmentXBinding? = null
  private val binding get() = _binding!!
  // inflate en onCreateView; _binding = null en onDestroyView
  ```
- **Sin arquitectura ni patrones de presentación**: NO MVVM, NO ViewModel/LiveData/Flow para la UI, NO Repository, NO UseCases, NO Hilt/Dagger. La lógica de autenticación va **directamente en los Fragments** para que sea fácil de leer y explicar.
- Las llamadas asíncronas de Credential Manager se hacen con **corrutinas** en `viewLifecycleOwner.lifecycleScope.launch { ... }` (no es un patrón de arquitectura, es lo que exige la API).
- Código **simple, lineal y muy comentado en español** (con KDoc por pantalla, igual que el repo).

## Método de integración: usa lo ACTUAL (Credential Manager)
El método antiguo de Google (`GoogleSignInClient` / `GoogleSignInOptions` de `play-services-auth`, con `startActivityForResult`/`onActivityResult`) **está obsoleto** (Google lo deprecó en 2024 y lo retira de Play Services Auth). **NO lo uses.**

Para el acceso con Google usa la API vigente que recomienda Firebase: **Credential Manager** (`androidx.credentials`) con **Sign in with Google** (`GetGoogleIdOption` → `GoogleIdTokenCredential`), y luego intercambia el idToken por una credencial de Firebase con `GoogleAuthProvider.getCredential(...)` + `signInWithCredential(...)`.

## Identidad del proyecto
- Nombre app: **AppAutenticacionFirebase**
- `namespace` / `applicationId`: **com.microsol.firebaseauth**
- `minSdk = 24`, Java 11, ViewBinding habilitado.

## Gradle (mismo estilo del repo, versiones estables)
- **Gradle Kotlin DSL** (`build.gradle.kts`) + **version catalog** en `gradle/libs.versions.toml` (referencia todo como `libs.xxx` / `libs.plugins.xxx`).
- **Usa versiones ESTABLES vigentes al momento de generar el proyecto** (AGP, Kotlin, Navigation, etc.). **Evita versiones alpha/beta** (mi repo tenía Navigation en alpha; aquí usa la estable). `compileSdk`/`targetSdk` estables (p. ej. 35/36).
- Plugins (en el catalog): `com.android.application`, el de Kotlin si corresponde a la versión de AGP que uses, **`androidx.navigation.safeargs.kotlin`** y **`com.google.gms.google-services`**.
- Dependencias (en el catalog):
  - `com.google.firebase:firebase-bom` (plataforma) + `com.google.firebase:firebase-auth` (sin versión, la fija el BoM).
  - `androidx.credentials:credentials`
  - `androidx.credentials:credentials-play-services-auth`
  - `com.google.android.libraries.identity.googleid:googleid`
  - `androidx.navigation:navigation-fragment-ktx` y `androidx.navigation:navigation-ui-ktx`
  - `androidx.core:core-ktx`, `androidx.appcompat:appcompat`, `com.google.android.material:material`, `androidx.constraintlayout:constraintlayout`, `androidx.fragment:fragment-ktx`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android`
  - Para la foto de perfil: **Coil** (`io.coil-kt:coil`) o Glide.
- **NO incluyas** `play-services-auth` (clásico), `GoogleSignInClient`, Picasso, multidex, Room ni KSP.

## Estructura esperada
```
app/
 └─ java/com/microsol/firebaseauth/
     ├─ MainActivity.kt              // host de navegación (una sola Activity)
     ├─ ui/login/LoginFragment.kt    // login con Google y con correo/contraseña
     └─ ui/home/HomeFragment.kt      // datos del usuario + cerrar sesión
 └─ res/
     ├─ layout/  activity_main.xml, fragment_login.xml, fragment_home.xml
     └─ navigation/ nav_graph.xml    // LoginFragment (inicio) -> HomeFragment
 └─ google-services.json            // lo coloca el estudiante (ver README)
```

## Pantallas y comportamiento

### LoginFragment (destino inicial del nav_graph)
- `EditText` para **correo** y **contraseña**, botón **"Ingresar"** y botón **"Registrarse"** (usa `signInWithEmailAndPassword` y `createUserWithEmailAndPassword`).
- Botón **"Iniciar sesión con Google"**.
- `ProgressBar` visible mientras se autentica.
- **Flujo de Google con Credential Manager:**
  1. `GetGoogleIdOption.Builder().setServerClientId(getString(R.string.default_web_client_id))...build()` (usa el **Web Client ID**, no el de Android).
  2. `GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()`.
  3. `CredentialManager.create(requireContext()).getCredential(requireContext(), request)` dentro de `viewLifecycleOwner.lifecycleScope.launch`; maneja `GetCredentialException` (cancelado / sin cuentas).
  4. Si la credencial es `CustomCredential` de tipo `TYPE_GOOGLE_ID_TOKEN_CREDENTIAL`, obtén el idToken con `GoogleIdTokenCredential.createFrom(credential.data).idToken`.
  5. `GoogleAuthProvider.getCredential(idToken, null)` y `FirebaseAuth.getInstance().signInWithCredential(...)`.
- Muestra errores con `Snackbar`/`Toast` claros.
- Si el login es exitoso, **navega a HomeFragment** con la acción Safe Args del `nav_graph` (limpia el back stack con `popUpTo`/`setPopUpTo` para no volver a Login con el botón atrás).
- En `onViewCreated`, si `FirebaseAuth.getInstance().currentUser != null`, navega directo a Home (sesión ya iniciada).

### HomeFragment
- Muestra los datos de `FirebaseAuth.getInstance().currentUser`: **nombre** (`displayName`), **correo** (`email`) y **foto** (`photoUrl`) en un `ImageView` (cárgala con Coil/Glide; comenta por qué se necesita una librería de imágenes).
- Botón **"Cerrar sesión"** que:
  1. `FirebaseAuth.getInstance().signOut()`.
  2. Limpia las credenciales con `CredentialManager.create(requireContext()).clearCredentialState(ClearCredentialStateRequest())` dentro de `lifecycleScope.launch` (recomendado por Credential Manager).
  3. Navega de vuelta a LoginFragment limpiando el back stack.

## Requisitos pedagógicos
- KDoc por pantalla (qué hace, como en el repo) y comentarios en español explicando los pasos clave (configuración de Google, credencial, `signInWithCredential`, navegación, manejo de sesión).
- Funciones cortas y descriptivas (p. ej. `iniciarSesionConGoogle()`, `autenticarConFirebase(idToken)`, `mostrarDatosUsuario()`, `cerrarSesion()`).
- Sin abstracciones innecesarias. Prioriza que un estudiante lo entienda leyéndolo.

## Configuración que hará el estudiante (documéntalo en un README.md)
1. Crear el proyecto en la **consola de Firebase** y registrar la app con el package `com.microsol.firebaseauth`.
2. Obtener la **huella SHA-1** (panel Gradle → `app > Tasks > android > signingReport`) y pegarla en Firebase (necesaria para el acceso con Google).
3. Descargar **google-services.json** y colocarlo en `app/`. El plugin genera el recurso `R.string.default_web_client_id` (Web Client ID) que usa Credential Manager.
4. En **Authentication → Sign-in method**, habilitar **Correo electrónico/contraseña** y **Google**.
5. Sincronizar Gradle y ejecutar la app.

## Criterios de aceptación (checklist)
- [ ] El proyecto compila (solo faltaría el `google-services.json`, no por errores de código).
- [ ] Gradle Kotlin DSL + version catalog, con versiones estables (sin alphas).
- [ ] Una sola Activity + Navigation Component con `nav_graph.xml` (Login → Home) usando **Safe Args**.
- [ ] **ViewBinding** con el patrón `_binding`/`binding`.
- [ ] Login con **correo/contraseña** y con **Google vía Credential Manager** (`GetGoogleIdOption` + `signInWithCredential`).
- [ ] HomeFragment muestra nombre, correo y foto, y permite **cerrar sesión** (`signOut` + `clearCredentialState`).
- [ ] Si ya hay sesión activa, entra directo a Home.
- [ ] **Sin** ViewModel, LiveData, Repository, UseCases, DI ni Compose.
- [ ] **Sin** `GoogleSignInClient`/`play-services-auth`/`onActivityResult` obsoletos ni `findViewById`.
- [ ] Código comentado en español y `README.md` con la configuración de Firebase.

## Qué NO hacer
- No uses Jetpack Compose, Java, ViewModel/MVVM, Repository, UseCases, inyección de dependencias ni capas de arquitectura.
- No uses la API obsoleta `GoogleSignInClient`/`GoogleSignInOptions`/`play-services-auth`/`onActivityResult`. Usa Credential Manager.
- No uses `findViewById` ni `multidex`.
- No agregues funcionalidades extra (chats, bases de datos, etc.). Mantén el alcance en autenticación.

Cuando termines, dame un resumen breve de los archivos creados y los pasos exactos para correrlo.