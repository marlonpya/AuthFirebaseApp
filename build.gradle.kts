// Archivo de build de nivel superior: aquí se declaran los plugins comunes a todos los módulos.
// Con `apply false` solo se "registran" las versiones; cada módulo (p. ej. :app) decide si los aplica.
plugins {
    alias(libs.plugins.android.application) apply false
    // Safe Args: genera las clases ...Directions para navegar de forma segura entre destinos.
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    // Plugin de Google Services: procesa google-services.json y genera R.string.default_web_client_id.
    alias(libs.plugins.google.gms.google.services) apply false
}
