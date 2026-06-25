# AppAutenticacionFirebase

App Android **didáctica** para aprender a integrar **Firebase Authentication**:

- Inicio de sesión con **correo y contraseña**.
- Inicio de sesión con **Google**, usando la API vigente **Credential Manager**
  (`androidx.credentials` + *Sign in with Google*), **no** el obsoleto `GoogleSignInClient`.
- Pantalla de inicio (Home) con los **datos del usuario** (nombre, correo y foto) y botón de **cerrar sesión**.

El foco es enseñar la integración de Firebase Auth, por eso el código es **simple, lineal y muy comentado**,
sin ViewModel, Repository, casos de uso, inyección de dependencias ni Jetpack Compose.

## Tecnologías

- **Kotlin** + UI en **XML** (layouts tradicionales).
- **Una sola Activity** + **Navigation Component** (`nav_graph.xml`) con **Safe Args** (Login → Home).
- **ViewBinding** (patrón `_binding`/`binding`, sin `findViewById`).
- **Firebase Authentication** (`firebase-bom` + `firebase-auth`).
- **Credential Manager** (`androidx.credentials`, `credentials-play-services-auth`, `googleid`).
- **Coil** para cargar la foto de perfil.
- Gradle **Kotlin DSL** + **version catalog** (`gradle/libs.versions.toml`).

## Estructura

```
app/
 └─ java/com/microsol/authfirebaseapp/
     ├─ MainActivity.kt              // host de navegación (única Activity)
     ├─ ui/login/LoginFragment.kt    // login con Google y con correo/contraseña
     └─ ui/home/HomeFragment.kt      // datos del usuario + cerrar sesión
 └─ res/
     ├─ layout/      activity_main.xml, fragment_login.xml, fragment_home.xml
     └─ navigation/  nav_graph.xml   // LoginFragment (inicio) -> HomeFragment
 └─ google-services.json            // lo coloca el estudiante (ver abajo)
```

## Configuración en Firebase (la haces tú)

1. Entra a la **consola de Firebase** (https://console.firebase.google.com), crea un proyecto y
   **registra una app Android** con el package:

   ```
   com.microsol.authfirebaseapp
   ```

2. Obtén la **huella SHA-1** de tu llave de depuración (necesaria para el acceso con Google):
   - En Android Studio, panel **Gradle** → `app > Tasks > android > signingReport`.
   - Copia el valor **SHA-1** de la variante `debug` y pégalo en la configuración de la app en Firebase.

3. Descarga **`google-services.json`** desde la consola y colócalo en la carpeta **`app/`**.
   - El plugin `com.google.gms.google-services` lo procesa y genera automáticamente el recurso
     **`R.string.default_web_client_id`** (el *Web Client ID*) que usa Credential Manager.
   - ⚠️ Sin este archivo el proyecto **no compila** (es lo único que falta, no hay errores de código).

4. En **Authentication → Sign-in method**, habilita:
   - **Correo electrónico/contraseña**
   - **Google**

5. **Sincroniza Gradle** y ejecuta la app.

## Ejecutar

1. Coloca `google-services.json` en `app/` (paso anterior).
2. Sincroniza el proyecto con Gradle.
3. Ejecuta en un emulador o dispositivo con **Google Play Services** (necesario para el acceso con Google).

## Notas

- El idToken de Google se pide con el **Web Client ID**, no con el ID de Android.
- Al cerrar sesión se llama a `signOut()` y a `clearCredentialState(...)` para limpiar la credencial guardada.
- Si ya existe una sesión activa, la app entra directo a Home al abrir.
