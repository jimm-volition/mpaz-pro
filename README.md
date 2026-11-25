# MPAZ PRO (Android)

Cliente móvil del caso **MPAZ PRO** (2° básico · DSY1105). La app se conecta al **API Gateway** (`:8080`); no llama directo a cada microservicio.

Repositorio: [jimm-volition/mpaz-pro](https://github.com/jimm-volition/mpaz-pro)

Backend (Docker): carpeta hermana  
`../../proyecto/Backend-Aplicaciones` (dentro del workspace EA3).

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
- APK release firmada (`mpaz-pro.jks` local; **no** subir al repo)

---

## Cómo correr (defensa / celular)

### 1. Backend

```bash
cd ../../proyecto/Backend-Aplicaciones
# Tener .env con secretos MySQL/JWT (ver .env.example)
docker compose up -d
```

Probar: [http://127.0.0.1:8080/actuator/health](http://127.0.0.1:8080/actuator/health) → `"status":"UP"`.

| Servicio | Puerto host |
|---|---|
| gateway | 8080 |
| ms-admin | 8081 |
| ms-contenidos | 8082 |
| ms-actividades-evaluaciones | 8083 |
| ms-progreso | 8084 |
| MySQL | 127.0.0.1:3307 |

### 2. Túnel USB (celular físico)

La release apunta a `http://127.0.0.1:8080`. En el teléfono eso solo funciona con **adb reverse**:

1. USB + depuración USB.
2. Doble clic en `adb-reverse-mpaz.bat` (elige el Samsung e ignora iVCam/Softwiner).
3. Debe listar `tcp:8080`. Si desconectás el cable, volvé a ejecutar el bat.

### 3. Instalar APK

- Archivo: `MPAZ-Pro-release.apk` (o `app/build/outputs/apk/release/app-release.apk`).
- Generar de nuevo:

```bash
.\gradlew.bat :app:assembleRelease --no-configuration-cache
```

Requiere `keystore.properties` + `mpaz-pro.jks` en la raíz del módulo Android (gitignored).

### 4. Emulador

También puede usar `127.0.0.1:8080` **si** corrés `adb reverse tcp:8080 tcp:8080` hacia el emulador.

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
- Si la app dice *failed to connect to 127.0.0.1:8080*: Docker UP + `adb-reverse-mpaz.bat`.
