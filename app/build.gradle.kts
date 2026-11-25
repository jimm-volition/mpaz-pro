import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val urlBackend = providers.gradleProperty("mpazBackendUrl")
    .orElse("http://127.0.0.1:18080")
    .get()

val puertoBackend = providers.gradleProperty("mpazHostPort")
    .orElse("18080")
    .get()

android {
    namespace = "com.example.pruebaaplicacionesmoviles"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pruebaaplicacionesmoviles"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "MPAZ_BACKEND_URL",
            "\"$urlBackend\""
        )
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(
                    keystoreProperties["storeFile"] as String
                )
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("io.mockk:mockk-jvm:1.14.11")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val adbSdk = androidComponents.sdkComponents.adb

val prepararConexionAndroid = tasks.register("prepararConexionAndroid") {

    doLast {
        if (urlBackend == "http://127.0.0.1:18080") {

            val adb = adbSdk.get().asFile.absolutePath

            fun ejecutar(vararg argumentos: String): Pair<Int, String> {

                val proceso = ProcessBuilder(
                    listOf(adb) + argumentos.toList()
                )
                    .redirectErrorStream(true)
                    .start()

                val salida = proceso.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                return proceso.waitFor() to salida
            }

            val (resultado, salida) = ejecutar("devices")

            if (resultado != 0) {

                logger.warn(salida)

            } else {

                val dispositivos = salida.lineSequence()
                    .map { it.trim().split(Regex("\\s+")) }
                    .filter {
                        it.size >= 2 && it[1] == "device"
                    }
                    .map { it[0] }
                    .toList()

                if (dispositivos.isEmpty()) {
                    logger.warn("No hay dispositivos Android conectados.")
                }

                dispositivos.forEach { dispositivo ->

                    val (codigo, mensaje) = ejecutar(
                        "-s",
                        dispositivo,
                        "reverse",
                        "tcp:18080",
                        "tcp:$puertoBackend"
                    )

                    if (codigo == 0) {
                        logger.lifecycle(
                            "Conexion MPAZ preparada: $dispositivo"
                        )
                    } else {
                        logger.warn(mensaje)
                    }
                }
            }
        }
    }
}

tasks.matching {
    it.name == "preDebugBuild"
}.configureEach {
    dependsOn(prepararConexionAndroid)
}