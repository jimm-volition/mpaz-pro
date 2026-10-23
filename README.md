# MPAZ PRO

Aplicación Android para **2° básico** (caso MPAZ PRO — DSY1105). Permite a la UTP autorizar y publicar unidades, al docente asignarlas a estudiantes y controlar intentos de prueba final, y al alumno consultar material, responder actividades y ver retroalimentación.

Repositorio: [jimm-volition/mpaz-pro](https://github.com/jimm-volition/mpaz-pro)

## Tecnologías

- Kotlin
- Jetpack Compose + Material 3
- Arquitectura **MVVM**
- Persistencia local con **Room** (SQLite)
- Coroutines + Flow
- Recursos nativos: **Photo Picker** (UTP) y **TextToSpeech** (contenido del estudiante)

## Roles y flujo

| Rol | Usuario de prueba | Qué puede hacer |
|---|---|---|
| UTP | `UTP01` | Autorizar, publicar o deshabilitar unidades; asociar imagen |
| Docente | `DOC01` | Asignar unidades a estudiantes, definir fechas e intentos, ver resultados |
| Estudiante | `EST01` | Ver unidades asignadas, estudiar contenido, rendir práctica/prueba |

**Login:** ingresar el alias y seleccionar el rol correspondiente (datos ficticios; no hay contraseña institucional).

### Flujo de demostración sugerido

1. Ingresar como `UTP01` → autorizar/publicar una unidad.
2. Salir e ingresar como `DOC01` → crear asignación para `EST01` (checkbox, fechas, switch “disponible”).
3. Salir e ingresar como `EST01` → abrir la asignatura → unidad → actividades → responder.
4. Volver como `DOC01` → revisar intentos en resultados.

## Cómo ejecutar

1. Clonar el repositorio.
2. Abrir el proyecto en **Android Studio**.
3. Esperar el sync de Gradle (`local.properties` se genera en tu máquina con la ruta del SDK).
4. Ejecutar en emulador o dispositivo (API 24+).

Si cambias el esquema de Room durante el desarrollo, desinstala la app una vez para recrear la base (hay `fallbackToDestructiveMigration` en debug/MVP).

## Estructura del proyecto

```
app/src/main/java/com/example/pruebaaplicacionesmoviles/
├── MainActivity.kt              # Navegación por rol
├── MpazApp.kt                   # Application + repositorio + semilla
├── data/
│   ├── model/                   # Modelos del dominio (contrato)
│   ├── local/                   # Entities, DAO, Database, DatosSemilla
│   └── repository/              # MpazRepository + RoomMpazRepository
├── viewmodel/                   # Login, UTP, Docente, Actividad
├── ui/
│   ├── screens/                 # Pantallas Compose
│   ├── components/              # Componentes reutilizables
│   └── theme/                   # Colores y tema MPAZ
```

## Organización del equipo

| Integrante | Responsabilidad principal |
|---|---|
| Juan Mendoza | Modelos, Room, repositorio, `UtpScreen`, `DocenteScreen` y ViewModels asociados |
| Gabriel Valdes | Login, dashboard estudiante, unidades, actividad/resultado, TextToSpeech, navegación |

## Criterios técnicos cubiertos

- Navegación entre roles y vistas
- Formularios con validación (lógica en ViewModel, feedback visual en UI)
- Estado con `StateFlow` / Compose
- Animaciones funcionales
- Persistencia local con Room
- MVVM
- Dos recursos nativos (galería / TTS)

## Notas

- Usuarios y contenidos son **ficticios** (semilla en `DatosSemilla`).
- No se usa cámara, GPS ni micrófono.
- El switch “Disponible para el estudiante” en la asignación se persiste (`activa`) y filtra lo que ve el alumno.
