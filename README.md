[README-frontend.md](https://github.com/user-attachments/files/35433087/README-frontend.md)
# MPAZ PRO (Android)

Cliente móvil del caso **MPAZ PRO** (2° básico · DSY1105). La app se conecta al **API Gateway** (`:18080` en el PC); no llama directo a cada microservicio.

Repositorio: [jimm-volition/mpaz-pro](https://github.com/jimm-volition/mpaz-pro)

Backend: proyecto `Backend-Aplicaciones`, ejecutado mediante Docker Compose. Usar las ramas `fix` de ambos repositorios mientras las correcciones no se integren en `main`.

---

## Qué hace cada rol

| Rol | Usuario | Clave | Capacidad |
|---|---|---|---|
| UTP | `UTP01` | `demo1234` | Crear/editar unidades, OA y materiales; transiciones hasta **PUBLICADO**; subir recursos |
| Docente | `DOC01` | `demo1234` | Ver curso/estudiantes; asignar **objetivos** autorizados/publicados; resultados e informe PDF |
| Estudiante | `EST01` | `demo1234` | Ver unidades/materiales **PUBLICADOS**; prácticas y prueba final |

Login remoto: `POST /api/v1/auth/demo-login` (JWT). El token se envía como `Authorization: Bearer …`.

### Estados de contenido (UTP)

`BORRADOR → EN_REVISION → AUTORIZADO → PUBLICADO` (y `DESHABILITADO`).

- El **estudiante** solo ve lo **PUBLICADO**.
- El **docente** puede asignar objetivos en **AUTORIZADO** o **PUBLICADO**.
- Para publicar un OA, la unidad debe estar PUBLICADA; para publicar material, OA y unidad también.

### Datos demo semilla (backend)

- Curso `1` — *2° Básico A* (docente DOC01).
- Matrícula EST01 en ese curso.
- Unidad `10` *Idea principal (demo)* + OA **`31`** + material `31` (todo PUBLICADO).
- Actividad y prueba final asociadas al OA `31`.

---

## Stack Android

- Kotlin, Jetpack Compose, Material 3, MVVM
- Retrofit / OkHttp → gateway
- Room (persistencia local / apoyo)
- Coroutines + Flow
- Nativos: Photo Picker (UTP), TextToSpeech (estudiante), FileProvider (PDF docente)
- APK debug para desarrollo; release firmada con keystore local (**no** subir al repo)

---

## Cómo correr (emulador y celular físico)

### 1. Requisitos

- Android Studio compatible con el proyecto, SDK de Android 37 y Platform Tools (ADB).
- JDK compatible con Gradle/AGP, configurado en Android Studio (por ejemplo, JDK 21). No fijar rutas personales de Java en `gradle.properties`.
- Docker Desktop y el backend actualizado en la rama correspondiente.

### 2. Iniciar el backend

Abrir Docker Desktop y, en PowerShell desde la raíz del backend, ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
```

Comprobar el Gateway: [http://127.0.0.1:18080/actuator/health](http://127.0.0.1:18080/actuator/health) → `status: UP`.

| Servicio | Puerto del PC |
|---|---:|
| Gateway | 18080 |
| MySQL | 13307 |
| Microservicios | Red interna de Docker; sin puertos publicados |

### 3. Ejecutar en el emulador

1. Abrir este proyecto en Android Studio y sincronizar Gradle.
2. Iniciar el emulador desde Device Manager **antes** de presionar Run. Esperar que termine de arrancar.
3. Seleccionar el emulador y presionar Run.
4. Iniciar sesión con uno de los usuarios demo.

### 4. Ejecutar en un celular Android

1. Activar las opciones de desarrollador y la depuración USB.
2. Conectar el celular con un cable USB de datos y aceptar la autorización de depuración. En Xiaomi/Redmi, habilitar la instalación vía USB si es necesario.
3. Seleccionar el dispositivo en Android Studio y presionar Run.
4. La APK queda instalada en el celular. Se puede cerrar la vista de pantalla compartida y abrir la app desde su icono.

Mantener la conexión ADB al PC y el backend funcionando para consumir los servicios locales. No hace falta cambiar IP, puerto ni código entre emulador y celular por USB.

### 5. Si aparece un error de conexión

Con el dispositivo iniciado y autorizado, ejecutar desde la raíz del frontend:

```powershell
.\gradlew.bat :app:prepararConexionAndroid
```

También se puede ejecutar `adb-reverse-mpaz.bat`. La tarea configura `adb reverse` del puerto 18080 del dispositivo al puerto del Gateway del PC. Debe aparecer `Conexion MPAZ preparada` para el dispositivo.

Gradle intenta configurar el túnel durante `preDebugBuild`. Si el emulador aún no ha arrancado, el dispositivo se conecta después o se pierde la conexión ADB, ejecutar nuevamente la tarea. No hace falta recompilar solo por esto.

### 6. Generar APK

APK debug (firmada para desarrollo):

```powershell
.\gradlew.bat :app:assembleDebug
```

Archivo generado: `app/build/outputs/apk/debug/app-debug.apk`.

Para una APK release nueva:

```powershell
.\gradlew.bat :app:assembleRelease --no-configuration-cache
```

Requiere `keystore.properties` y el keystore privado configurados en la raíz del proyecto. No subir estos archivos. Una APK antigua generada antes del cambio de puerto debe recompilarse; instalar una APK por sí solo no crea el túnel ADB.

## Configuración centralizada de red

`core/network/ConfigBackend.kt` obtiene la URL desde `BuildConfig.MPAZ_BACKEND_URL`. Los clientes de login, contenidos, UTP y recursos comparten esta configuración.

- URL local predeterminada: `http://127.0.0.1:18080`.
- API REST: `http://127.0.0.1:18080/api/v1/`.
- `mpazHostPort`: puerto del Gateway en el PC (por defecto `18080`). Si se cambia el puerto externo del backend, definir esta propiedad en la configuración de Gradle local; el puerto del dispositivo puede seguir siendo 18080.
- `mpazBackendUrl`: URL alternativa para un backend accesible por red (por ejemplo, HTTPS en un servidor remoto). Se configura antes de compilar, sin editar Kotlin. La tarea de ADB solo prepara el túnel cuando se usa la URL local predeterminada.

Ejemplo para un puerto alternativo del Gateway:

```powershell
.\gradlew.bat :app:prepararConexionAndroid -PmpazHostPort=18081
```

La configuración predeterminada por USB se probó en emulador y Redmi 13C. La ejecución de una APK sin ADB requiere un backend accesible por red y no forma parte de la prueba local realizada.

---

## Guion de demo sugerido

1. **EST01** → Lenguaje → unidad *Idea principal (demo)* → material / actividades.
2. **DOC01** → usar OA **31** → crear asignación para EST01 (fechas, intentos, confirmación).
3. **UTP01** (si preguntan) → mostrar transición de estados hasta PUBLICADO.
4. Volver a **DOC01** → resultados / informe.

Guía impresa: `../../GUIA_DEFENSA_MPAZ_PRO.pdf` (workspace EA3).

---

## Estructura (app)

```
app/src/main/java/com/example/pruebaaplicacionesmoviles/
├── MainActivity.kt
├── MpazApp.kt
├── core/network/          # Retrofit: login, docente, actividades, UTP, contenidos
├── data/
│   ├── model/
│   ├── local/             # Room
│   └── repository/
├── viewmodel/
└── ui/screens/            # Login, dashboards, UTP remoto, Docente, Actividad, …
```

Tests unitarios (ej.): `ErroresAsignacionTest`, mensajes de envío — `app/src/test/…`.

---

## Organización del equipo

| Integrante | Enfoque |
|---|---|
| Juan Mendoza (Integrante B) | Gateway/actividades/progreso, pantallas UTP remoto / docente / actividades / informes, Docker, APK, contrato HTTP |
| Gabriel Valdes | Login/UI base estudiante, unidades, TTS, navegación |

---

## Criterios técnicos cubiertos

- Navegación por rol
- Formularios con validación (ViewModel + UI)
- Estado con StateFlow / Compose
- Animaciones funcionales
- Room + consumo remoto del contrato
- MVVM
- Recursos nativos (galería / TTS / compartir PDF)
- Backend multi-servicio detrás de gateway + Docker Compose

---

## Notas / seguridad

- Usuarios y contenidos de demo son **ficticios**.
- No subir al remoto: `.env`, `*.jks`, `keystore.properties`.
- Cleartext HTTP solo para demo local (`usesCleartextTraffic=true`).
- Si la app dice *failed to connect to 127.0.0.1:18080*: comprobar Docker y ejecutar `:app:prepararConexionAndroid` con el dispositivo conectado.
