plugins {
    alias(libs.plugins.android.application)
    // Safe Args (Kotlin): genera LoginFragmentDirections / HomeFragmentDirections para navegar.
    alias(libs.plugins.androidx.navigation.safeargs)
    // Google Services: lee google-services.json y crea R.string.default_web_client_id (Web Client ID).
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.microsol.authfirebaseapp"
    // core-ktx 1.19.0 exige compilar contra la API 37, así que usamos compileSdk 37.
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.microsol.authfirebaseapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // ViewBinding: acceso tipado a las vistas (sin findViewById ni Kotlin synthetics).
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX base + Material + ConstraintLayout
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)

    // Navigation Component (con Safe Args): una sola Activity + Fragments.
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase Authentication. El BoM (platform) fija la versión de firebase-auth.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Credential Manager + Sign in with Google (API vigente que recomienda Firebase).
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Corrutinas: requeridas por las llamadas suspend de Credential Manager.
    implementation(libs.kotlinx.coroutines.android)

    // Coil: carga la foto de perfil (URL remota) dentro de un ImageView.
    implementation(libs.coil)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
