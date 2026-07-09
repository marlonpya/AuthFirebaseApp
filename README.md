# 🔐 AppAutenticacionFirebase

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase_Auth-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Firestore](https://img.shields.io/badge/Cloud_Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Storage](https://img.shields.io/badge/Cloud_Storage-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Credential Manager](https://img.shields.io/badge/Credential_Manager-4285F4?style=for-the-badge&logo=google&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-24-success?style=for-the-badge)

> Proyecto **didáctico** para una clase de Android: aprende a integrar **Firebase Authentication**
> (correo/contraseña + Google vía *Credential Manager*), **Cloud Firestore** (MVVM + Repository) y
> **Cloud Storage** (fotos adjuntas), todo dentro de la misma app.

---

## 📖 Descripción

Este repo mezcla **dos paradigmas a propósito**, cada uno correspondiente a una lección distinta
del curso — no están unificados adrede:

- **Login/Home** (`ui/login`, `ui/home`): arquitectura **plana**. La lógica de autenticación vive
  directamente en los Fragments, **sin** ViewModel, Repository, casos de uso ni Jetpack Compose.
  Es la lección de "Firebase Auth básico".
- **Cursos/Tareas** (`domain/`, `data/`, `presentation/`): arquitectura **en capas** —
  MVVM + Repository, con `domain/` 100% Kotlin puro (sin imports de Android/Firebase), `data/`
  implementando los repositorios con Firestore y Storage, y `presentation/` con
  ViewModel + `StateFlow` + Fragment + Adapter. Es la lección de "Firestore + arquitectura".

Para el acceso con Google se usa la API **vigente** que recomienda Firebase —**Credential Manager**
(`androidx.credentials`) + *Sign in with Google*— y **no** el obsoleto `GoogleSignInClient` /
`onActivityResult` de Play Services Auth.

## ✨ Funcionalidades

**Autenticación**
- ✅ **Iniciar sesión con correo y contraseña** (`signInWithEmailAndPassword`).
- ✅ **Registrar** una cuenta nueva con correo y contraseña (`createUserWithEmailAndPassword`).
- ✅ **Iniciar sesión con Google** usando **Credential Manager** (`GetGoogleIdOption` → `idToken` →
  `signInWithCredential`).
- ✅ **Pantalla Home** con los datos del usuario: **nombre**, **correo** y **foto** de perfil.
- ✅ **Cerrar sesión** (`signOut()` + `clearCredentialState(...)`).
- ✅ **Sesión persistente**: si ya hay un usuario logueado, la app entra directo a Home.

**Cursos y Tareas (Cloud Firestore)**
- ✅ **Listar cursos** (catálogo global compartido entre usuarios autenticados).
- ✅ **Crear y editar** cursos.
- ✅ **Listar tareas de un curso**, **crear**, **editar**, **eliminar** y **alternar "completada"**.

**Fotos adjuntas a una Tarea (Cloud Storage)**
- ✅ Hasta **3 fotos por tarea**, cada una en su propia casilla del formulario.
- ✅ Selección con el **Photo Picker** del sistema (`ActivityResultContracts.PickVisualMedia`) —
  sin permisos de cámara ni de almacenamiento.
- ✅ Las fotos se suben a Storage y su URL se guarda en Firestore recién al tocar **"Guardar"**
  (evita archivos huérfanos si se abandona el formulario).
- ✅ Se puede **quitar** una foto individual o **reemplazarla**, tanto al crear como al editar.

## 📸 Capturas de Pantalla

| Login | Home | Cursos | Tareas |
|:-----:|:----:|:------:|:------:|
| _(agrega aquí tu captura)_ | _(agrega aquí tu captura)_ | _(agrega aquí tu captura)_ | _(agrega aquí tu captura)_ |

## 🧱 Estructura del Proyecto

> ℹ️ Fíjate que `ui/` es plano a propósito (lección de Auth básico) mientras que `domain/`, `data/`
> y `presentation/` están en capas (lección de Firestore + arquitectura). Ver "Descripción" arriba.

```
app/
 └─ 📂 java/com/microsol/authfirebaseapp/
     ├─ 📄 MainActivity.kt              // host de navegación (única Activity)
     ├─ 📂 ui/
     │   ├─ 📂 login/LoginFragment.kt   // login con Google y con correo/contraseña
     │   └─ 📂 home/HomeFragment.kt     // datos del usuario + cerrar sesión
     ├─ 📂 domain/                      // interfaces + modelos, sin imports de Android/Firebase
     │   ├─ 📂 model/       Curso.kt, Tarea.kt
     │   └─ 📂 repository/  CursoRepository.kt, TareaRepository.kt, StorageRepository.kt
     ├─ 📂 data/                        // implementación con Firestore + Storage
     │   ├─ 📂 model/       CursoDto.kt, TareaDto.kt          (@DocumentId, Timestamp)
     │   ├─ 📂 mapper/      CursoMapper.kt, TareaMapper.kt    (DTO ↔ dominio)
     │   └─ 📂 repository/  Firestore{Curso,Tarea}RepositoryImpl.kt, StorageRepositoryImpl.kt
     └─ 📂 presentation/                // MVVM: ViewModel + StateFlow + Fragment + Adapter
         ├─ 📂 cursos/       CursosViewModel, CursosState, CursosFragment, CursosAdapter, form/
         └─ 📂 tareas/       TareasViewModel, TareasState, TareasFragment, TareasAdapter, form/
                              // form/ incluye FotoSlot.kt (estado de las 3 casillas de foto)
 └─ 📂 res/
     ├─ 📂 layout/      fragment_login.xml, fragment_home.xml, fragment_cursos.xml,
     │                   fragment_tarea_form.xml (con las 3 casillas de foto), etc.
     └─ 📂 navigation/  nav_graph.xml   // Login → Home → Cursos → Tareas → TareaForm
 └─ 📄 google-services.json             // lo colocas tú (ver "Configuración de Firebase")
firestore.rules                          // reglas de Firestore (copiar a la consola, no auto-deploy)
storage.rules                            // reglas de Storage (copiar a la consola, no auto-deploy)
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

## 🗂️ Flujo de Navegación: Cursos → Tareas → Foto

```
 HomeFragment ──► CursosFragment ──► TareasFragment ──► TareaFormFragment
                  (lista, MVVM)      (lista de un       (crear/editar +
                       │             curso, MVVM)        hasta 3 fotos)
                       │                   │                   │
                 CursoFormFragment   FAB "+" / editar    Photo Picker por
                 (crear/editar)      ───────────────►    casilla (vista previa
                                                          local, no sube aún)
                                                                │
                                                    "Guardar" ──┼─► sube fotos a
                                                                │   Storage + guarda
                                                                │   imagenesUrls en
                                                                │   Firestore
                                                                ▼
                                                        popBackStack() a Tareas
```

## 🧩 Conceptos Aplicados

| Concepto | Dónde se usa | Para qué |
|----------|--------------|----------|
| **Navigation Component + Safe Args** | `nav_graph.xml`, `*FragmentDirections` | Navegar entre pantallas de forma type-safe |
| **ViewBinding** | patrón `_binding`/`binding` en todos los Fragments | Acceso tipado a vistas sin `findViewById` |
| **Credential Manager** | `LoginFragment.iniciarSesionConGoogle()` | Acceso con Google (API vigente) |
| **Firebase Auth** | `FirebaseAuth.getInstance()` | Sesión, login y registro |
| **Cloud Firestore** | `data/repository/Firestore{Curso,Tarea}RepositoryImpl.kt` | Persistir cursos y tareas (colecciones planas + FK) |
| **MVVM + Repository** | `domain/`, `data/`, `presentation/{cursos,tareas}` | Separar UI, lógica de negocio y acceso a datos |
| **StateFlow + repeatOnLifecycle** | `*ViewModel.estado`, `observarEstado()` en los Fragments | Estado de UI reactivo (`Loading`/`Exito`/`Error`) |
| **Corrutinas + `.await()`** | `kotlinx-coroutines-play-services` en los repositorios | Convertir `Task<T>` de Firebase a `suspend fun` |
| **ListAdapter + DiffUtil** | `CursosAdapter`, `TareasAdapter` | Listas eficientes en RecyclerView |
| **Cloud Storage** | `data/repository/StorageRepositoryImpl.kt` | Subir/borrar las fotos adjuntas a una tarea |
| **Photo Picker** | `TareaFormFragment` (`ActivityResultContracts.PickVisualMedia`) | Elegir fotos sin permisos de almacenamiento/cámara |
| **Coil** | `imageView.load(url)` en Home y en las casillas de foto | Descargar/mostrar imágenes remotas y locales |
| **Version catalog** | `gradle/libs.versions.toml` | Centralizar versiones y dependencias |

## 🛠️ Tech Stack

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Kotlin (integrado en AGP 9) | AGP `9.2.1` | Lenguaje |
| Firebase BoM (Auth + Firestore + Storage) | `34.4.0` | Autenticación, base de datos y almacenamiento |
| androidx.credentials | `1.5.0` | Credential Manager |
| googleid | `1.1.1` | *Sign in with Google* (idToken) |
| Navigation + Safe Args | `2.10.0-alpha05` | Navegación entre Fragments |
| Coroutines (+ `play-services`) | `1.10.1` | Asincronía y `.await()` sobre `Task<T>` |
| Coil | `2.7.0` | Carga de imágenes (remotas y locales) |
| RecyclerView | `1.4.0` | Listas de Cursos/Tareas |
| Lifecycle ViewModel/Runtime | `2.9.0` | ViewModel + `StateFlow` + `repeatOnLifecycle` |
| compileSdk / minSdk / targetSdk | `37` / `24` / `36` | SDK objetivo / mínimo / target |

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos
- ✅ Android Studio actualizado.
- ✅ Un emulador o dispositivo **con Google Play Services** (necesario para el acceso con Google).
- ✅ Un proyecto en la **consola de Firebase**, en **plan Blaze** (requerido por Cloud Storage).

### Pasos
1. Clona o abre el proyecto en Android Studio.
2. Completa la **Configuración de Firebase** (sección siguiente) y coloca `google-services.json` en `app/`.
3. **Sincroniza Gradle** (File → *Sync Project with Gradle Files*).
4. Ejecuta la app (▶️ o `Shift + F10`).

> ⚠️ **Si trabajas desde WSL/Linux**: el proyecto no compila desde ahí si el SDK de Android
> configurado en `local.properties` apunta a una instalación de Windows (los binarios `aapt`/`aapt2`
> serían `.exe`, no ejecutables en Linux). Compila y ejecuta desde **Android Studio en Windows**
> (o `gradlew.bat` en cmd/PowerShell).

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
4. En **Firestore Database**, crea la base de datos (modo producción) y copia el contenido de
   `firestore.rules` (raíz del repo) a la pestaña **Reglas**.
5. En **Storage**, crea el bucket — **requiere plan Blaze** (pago por uso; hay cuota gratuita, así
   que no necesariamente genera costo, pero sin cuenta de facturación vinculada no funciona) — y
   copia el contenido de `storage.rules` (raíz del repo) a la pestaña **Reglas**.
6. Descarga **`google-services.json`** y colócalo en la carpeta **`app/`**.
   - El plugin `com.google.gms.google-services` genera el recurso **`R.string.default_web_client_id`**
     (el *Web Client ID*) que usa Credential Manager, y el campo `storage_bucket` que usa
     `FirebaseStorage.getInstance()` sin necesidad de configurarlo a mano en el código.
   - ⚠️ **Importante:** habilita Google **antes** de descargar el JSON; de lo contrario el bloque
     `oauth_client` viene vacío y no se generará `default_web_client_id` (no compilará el login con Google).
   - ⚠️ Si habilitas Firestore/Storage **después** de haber descargado el JSON, vuelve a descargarlo
     (si no, `storage_bucket` puede faltar en el archivo).
7. **Sincroniza Gradle** y ejecuta. 🎉

## 🗃️ Modelo de datos

Colecciones raíz de Firestore, planas con FK (**no** subcolecciones):

- **`cursos`**: `id`, `nombre`, `descripcion`, `fechaCreacion`. Catálogo **global**: cualquier
  usuario autenticado ve y edita todos los cursos.
- **`tareas`**: `id`, `cursoId` (FK a `cursos.id`), `titulo`, `completada`, `fechaLimite`,
  `imagenesUrls` (lista de 0 a 3 URLs de Storage). Se filtran con `whereEqualTo("cursoId", cursoId)`.

Storage guarda las fotos bajo `tareas/{tareaId}/{uuid}.jpg` — cada foto tiene un nombre único, así
que agregar o quitar una no afecta a las demás de la misma tarea.

## 🎓 Propósito de Aprendizaje

Al terminar este proyecto deberías poder:
- Configurar Firebase Authentication en una app Android desde cero.
- Implementar **Sign in with Google** con la API moderna de **Credential Manager**.
- Manejar login/registro con **correo y contraseña**.
- Leer datos del usuario autenticado y **cerrar sesión** correctamente.
- Navegar entre Fragments con **Navigation Component + Safe Args** y usar **ViewBinding**.
- Modelar datos en **Cloud Firestore** (colecciones planas, FK, DTO ↔ dominio) y aplicar
  **MVVM + Repository** para separar UI, lógica de negocio y acceso a datos.
- Subir y gestionar archivos con **Cloud Storage**, usando el **Photo Picker** del sistema en vez
  de permisos legacy de cámara/almacenamiento.

## 🔮 Mejoras Futuras

- [ ] Recuperación de contraseña (`sendPasswordResetEmail`).
- [ ] Verificación de correo electrónico.
- [ ] Más proveedores (Facebook, GitHub, anónimo).
- [ ] Validación de formularios y mensajes de error más detallados.
- [ ] *Nonce* en el flujo de Google para mayor seguridad.
- [ ] Miniatura de la foto de la tarea en `TareasAdapter`/`item_tarea.xml` (hoy solo se ve en el formulario).
- [ ] Tomar la foto con la cámara (alternativa al Photo Picker, requeriría permiso `CAMERA`).
- [ ] Comprimir/redimensionar la imagen antes de subirla a Storage.
- [ ] Eliminar cursos (hoy solo se pueden crear/editar).

## 📝 Notas

- El **idToken** de Google se pide con el **Web Client ID**, no con el ID de Android.
- Al cerrar sesión se llama a `signOut()` y a `clearCredentialState(...)` para limpiar la credencial guardada.
- `firestore.rules` y `storage.rules` **no se despliegan automáticamente** desde este repo: hay
  que copiarlos manualmente a la consola de Firebase, o usar `firebase deploy --only firestore:rules,storage`
  si se agrega la Firebase CLI al proyecto.
- Cloud Storage exige **plan Blaze** vinculado a una cuenta de facturación, aunque el uso se
  mantenga dentro de la cuota gratuita — verifícalo en la consola antes de probar la subida de fotos.
- Proyecto con fines educativos. 📚
