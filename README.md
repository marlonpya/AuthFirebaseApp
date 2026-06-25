# 🔐 AppAutenticacionFirebase

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase_Auth-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Credential Manager](https://img.shields.io/badge/Credential_Manager-4285F4?style=for-the-badge&logo=google&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-24-success?style=for-the-badge)

> Proyecto **didáctico** para una clase de Android: aprende a integrar **Firebase Authentication**
> con **inicio de sesión por Google** (vía la API vigente *Credential Manager*) y por **correo/contraseña**.

---

## 📖 Descripción

App sencilla con **una sola Activity** y dos pantallas que enseña, de principio a fin, cómo autenticar
usuarios con Firebase en Android. El foco es la **integración de Firebase Auth**, por eso el código es
**simple, lineal y muy comentado en español**: la lógica de autenticación vive directamente en los
Fragments, **sin** ViewModel, Repository, casos de uso, inyección de dependencias ni Jetpack Compose.

Para el acceso con Google se usa la API **vigente** que recomienda Firebase —**Credential Manager**
(`androidx.credentials`) + *Sign in with Google*— y **no** el obsoleto `GoogleSignInClient` /
`onActivityResult` de Play Services Auth.

## ✨ Funcionalidades

- ✅ **Iniciar sesión con correo y contraseña** (`signInWithEmailAndPassword`).
- ✅ **Registrar** una cuenta nueva con correo y contraseña (`createUserWithEmailAndPassword`).
- ✅ **Iniciar sesión con Google** usando **Credential Manager** (`GetGoogleIdOption` → `idToken` →
  `signInWithCredential`).
- ✅ **Pantalla Home** con los datos del usuario: **nombre**, **correo** y **foto** de perfil.
- ✅ **Cerrar sesión** (`signOut()` + `clearCredentialState(...)`).
- ✅ **Sesión persistente**: si ya hay un usuario logueado, la app entra directo a Home.

## 📸 Capturas de Pantalla

| Login | Home |
|:-----:|:----:|
| _(agrega aquí tu captura)_ | _(agrega aquí tu captura)_ |

## 🧱 Estructura del Proyecto

> ℹ️ A diferencia de un proyecto con Clean Architecture, aquí la estructura es **plana y por feature**
> a propósito, para que un estudiante la entienda de un vistazo.

```
app/
 └─ 📂 java/com/microsol/authfirebaseapp/
     ├─ 📄 MainActivity.kt              // host de navegación (única Activity)
     ├─ 📂 ui/login/
     │   └─ 📄 LoginFragment.kt         // login con Google y con correo/contraseña
     └─ 📂 ui/home/
         └─ 📄 HomeFragment.kt          // datos del usuario + cerrar sesión
 └─ 📂 res/
     ├─ 📂 layout/      activity_main.xml, fragment_login.xml, fragment_home.xml
     └─ 📂 navigation/  nav_graph.xml   // LoginFragment (inicio) → HomeFragment
 └─ 📄 google-services.json             // lo colocas tú (ver "Configuración de Firebase")
```

## 🔄 Flujo de Autenticación

```
                ┌─────────────────────────────┐
                │        LoginFragment        │
                │  (destino inicial del graph)│
                └──────────────┬──────────────┘
            correo/contraseña  │  Google (Credential Manager)
                               │
      signInWithEmailAndPassword│  GetGoogleIdOption → idToken
      createUserWithEmail...    │  → GoogleAuthProvider.getCredential
                               ▼
                     FirebaseAuth.signInWithCredential / ...Email
                               │
                       ¿éxito? │ (back stack limpio con popUpTo)
                               ▼
                ┌─────────────────────────────┐
                │         HomeFragment        │
                │  nombre · correo · foto     │
                │  └─ Cerrar sesión ──────────┼─► signOut() + clearCredentialState()
                └─────────────────────────────┘
```

## 🧩 Conceptos Aplicados

| Concepto | Dónde se usa | Para qué |
|----------|--------------|----------|
| **Navigation Component + Safe Args** | `nav_graph.xml`, `LoginFragmentDirections` | Navegar Login → Home de forma type-safe |
| **ViewBinding** | patrón `_binding`/`binding` en los Fragments | Acceso tipado a vistas sin `findViewById` |
| **Credential Manager** | `LoginFragment.iniciarSesionConGoogle()` | Acceso con Google (API vigente) |
| **Corrutinas** | `viewLifecycleOwner.lifecycleScope.launch { }` | Llamadas `suspend` de Credential Manager |
| **Firebase Auth** | `FirebaseAuth.getInstance()` | Sesión, login y registro |
| **Coil** | `imageView.load(photoUrl)` en Home | Descargar/mostrar la foto de perfil |
| **Version catalog** | `gradle/libs.versions.toml` | Centralizar versiones y dependencias |

## 🛠️ Tech Stack

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Kotlin (integrado en AGP 9) | AGP `9.2.1` | Lenguaje |
| Firebase BoM + Auth | `34.4.0` | Autenticación |
| androidx.credentials | `1.5.0` | Credential Manager |
| googleid | `1.1.1` | *Sign in with Google* (idToken) |
| Navigation + Safe Args | `2.10.0-alpha05` | Navegación entre Fragments |
| Coroutines | `1.10.1` | Asincronía |
| Coil | `2.7.0` | Carga de imágenes |
| compileSdk / minSdk | `37` / `24` | SDK objetivo / mínimo |

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos
- ✅ Android Studio actualizado.
- ✅ Un emulador o dispositivo **con Google Play Services** (necesario para el acceso con Google).
- ✅ Un proyecto en la **consola de Firebase**.

### Pasos
1. Clona o abre el proyecto en Android Studio.
2. Completa la **Configuración de Firebase** (sección siguiente) y coloca `google-services.json` en `app/`.
3. **Sincroniza Gradle** (File → *Sync Project with Gradle Files*).
4. Ejecuta la app (▶️ o `Shift + F10`).

## 🔥 Configuración de Firebase (la haces tú)

1. En la **consola de Firebase** (https://console.firebase.google.com) crea un proyecto y
   **registra una app Android** con el package:
   ```
   com.microsol.authfirebaseapp
   ```
2. Obtén la **huella SHA-1** de tu llave de depuración (necesaria para Google):
   - Panel **Gradle** → `app > Tasks > android > signingReport`.
   - Copia el **SHA-1** de la variante `debug` y pégalo en la app dentro de Firebase.
3. En **Authentication → Sign-in method**, habilita:
   - 📧 **Correo electrónico/contraseña**
   - 🔵 **Google**
4. Descarga **`google-services.json`** y colócalo en la carpeta **`app/`**.
   - El plugin `com.google.gms.google-services` genera el recurso **`R.string.default_web_client_id`**
     (el *Web Client ID*) que usa Credential Manager.
   - ⚠️ **Importante:** habilita Google **antes** de descargar el JSON; de lo contrario el bloque
     `oauth_client` viene vacío y no se generará `default_web_client_id` (no compilará el login con Google).
5. **Sincroniza Gradle** y ejecuta. 🎉

## 🎓 Propósito de Aprendizaje

Al terminar este proyecto deberías poder:
- Configurar Firebase Authentication en una app Android desde cero.
- Implementar **Sign in with Google** con la API moderna de **Credential Manager**.
- Manejar login/registro con **correo y contraseña**.
- Leer datos del usuario autenticado y **cerrar sesión** correctamente.
- Navegar entre Fragments con **Navigation Component + Safe Args** y usar **ViewBinding**.

## 🔮 Mejoras Futuras

- [ ] Recuperación de contraseña (`sendPasswordResetEmail`).
- [ ] Verificación de correo electrónico.
- [ ] Más proveedores (Facebook, GitHub, anónimo).
- [ ] Validación de formularios y mensajes de error más detallados.
- [ ] *Nonce* en el flujo de Google para mayor seguridad.

## 📝 Notas

- El **idToken** de Google se pide con el **Web Client ID**, no con el ID de Android.
- Al cerrar sesión se llama a `signOut()` y a `clearCredentialState(...)` para limpiar la credencial guardada.
- Proyecto con fines educativos. 📚
