# My Car (MiCarro)

[![Android CI](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml/badge.svg)](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml)

Aplicación nativa de Android para el seguimiento de vehículos: kilometraje, historial de mantenimientos, exportación de datos y alertas preventivas.

> **Nota:** el proyecto está en desarrollo activo. Algunas pantallas aún son placeholders y la persistencia Room se conectará cuando se integren los módulos de vehículos y mantenimiento.

---

## Características

| Módulo | Estado | Descripción |
| --- | --- | --- |
| Dashboard | En construcción | Resumen del vehículo principal, próximos mantenimientos y alertas |
| Historial | Disponible | Búsqueda de servicios/talleres y listado de mantenimientos |
| Exportación CSV | Disponible | Comparte el historial con `FileProvider` |
| Configuración | Disponible | Borrado irreversible de todos los datos locales |
| Alertas | Infraestructura lista | Notificaciones programadas con WorkManager |
| Vehículos | Pendiente | Lista y alta de vehículos (rutas definidas) |
| Plan de mantenimiento | Pendiente | Planificación de servicios (ruta definida) |

Tipos de vehículo soportados en el modelo de dominio: **automóvil**, **camión** y **motocicleta**.

---

## Stack tecnológico

| Área | Tecnología | Versión |
| --- | --- | --- |
| Lenguaje | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 | BOM 2026.02.01 |
| Arquitectura | Clean Architecture + MVVM | — |
| DI | Hilt | 2.60.1 |
| Persistencia | Room | 2.6.1 |
| Asincronía | Coroutines + Flow | 1.7.3 |
| Navegación | Navigation Compose | 2.7.7 |
| Trabajo en segundo plano | WorkManager | 2.9.0 |
| Build | Android Gradle Plugin / Gradle | 9.3.2 / 9.5.0 |
| JDK | Java / toolchain Gradle | 17 |
| SDK | `minSdk` 24 · `targetSdk` / `compileSdk` 37 | — |

---

## Arquitectura

El código sigue **Clean Architecture** y la guía oficial de Android con **MVVM**:

```
┌─────────────────────────────────────────┐
│  UI (Compose + ViewModel)               │
│  feature/*/presentation                 │
├─────────────────────────────────────────┤
│  Domain (casos de uso + modelos)        │
│  independiente de Android               │
├─────────────────────────────────────────┤
│  Data (repositorios, Room, mappers)     │
│  Fake*Impl mientras se integra Room     │
└─────────────────────────────────────────┘
```

- **UI:** pantallas Compose, `UiState` inmutable y ViewModels con Hilt (`@HiltViewModel`).
- **Domain:** casos de uso puros (`GetDashboardSummaryUseCase`, `ExportHistoryToCsvUseCase`, `ClearAllDataUseCase`) y contratos de repositorio.
- **Data:** implementaciones inyectadas. Hoy Hilt enlaza `FakeVehicleRepositoryImpl` y `FakeMaintenanceRepositoryImpl`; Room (`micarro_db`) y las entidades ya existen para la integración posterior.
- **Core:** navegación, DI, WorkManager, tema y utilidades compartidas.

---

## Estructura del proyecto

```
My_Car/
├── .github/workflows/android_ci.yml   # CI: tests unitarios en Ubuntu
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/example/my_car/
│       │   ├── core/                  # DI, Room, navegación, workers, utils
│       │   ├── data/                  # entidades, mappers, repositorios
│       │   ├── domain/                # modelos, repositorios, casos de uso
│       │   ├── feature/               # dashboard, history, settings
│       │   └── ui/                    # tema, colores, StatusChip
│       ├── test/                      # pruebas unitarias (JUnit + coroutines-test)
│       └── androidTest/               # pruebas instrumentadas
├── gradle/libs.versions.toml          # catálogo de versiones
└── gradlew / gradlew.bat
```

Rutas de navegación (`Screen`):

| Ruta | Destino |
| --- | --- |
| `dashboard_screen` | Inicio (placeholder) |
| `history_screen` | Historial |
| `settings_screen` | Configuración |
| `vehicle_list_screen` | Lista de vehículos (placeholder) |
| `maintenance_plan_screen` | Plan de mantenimiento (placeholder) |

---

## Requisitos

- [Android Studio](https://developer.android.com/studio) (recomendado: última versión estable)
- JDK **17** (el daemon de Gradle usa toolchain 17)
- Un emulador o dispositivo físico con Android 7.0 (API 24) o superior

### Windows

Clona el proyecto en una ruta **sin espacios ni caracteres especiales** (por ejemplo `C:\Proyecto\My_Car`). Rutas con espacios pueden romper workers de Gradle e IPC.

Si la caché de Gradle en el perfil de usuario da problemas, usa un directorio dedicado:

```powershell
.\gradlew.bat test -g C:\gradle_home
```

---

## Cómo ejecutar

### Android Studio

1. Clona el repositorio:
   ```bash
   git clone https://github.com/Santiagomunozma/My_Car.git
   ```
2. Abre la carpeta del proyecto en Android Studio.
3. Espera a que Gradle sincronice.
4. Selecciona un dispositivo o emulador y pulsa **Run**.

### Línea de comandos

```powershell
# Windows
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug

# Linux / macOS
./gradlew assembleDebug
./gradlew installDebug
```

El `applicationId` es `com.example.my_car` y la versión actual es **1.0** (`versionCode` 1).

---

## Pruebas

```powershell
# Todas las pruebas unitarias
.\gradlew.bat test

# Pruebas de un módulo concreto
.\gradlew.bat :app:testDebugUnitTest

# Pruebas instrumentadas (dispositivo o emulador conectado)
.\gradlew.bat connectedAndroidTest
```

Pruebas unitarias actuales:

- `GetDashboardSummaryUseCaseTest` — resumen del dashboard sin vehículos
- `HistoryViewModelTest` — el filtro de búsqueda refleja el texto ingresado

---

## Integración continua

El workflow [`.github/workflows/android_ci.yml`](.github/workflows/android_ci.yml) se ejecuta en cada **push** y **pull request** hacia `develop` y `main`:

1. Checkout del código
2. JDK 17 (Temurin) con caché de Gradle
3. `./gradlew test --stacktrace`

---

## Diseño UI/UX

La paleta, radios y componentes reutilizables están centralizados. **Ningún módulo debe definir colores HEX ni radios propios.**

| Token | Uso | Color |
| --- | --- | --- |
| `PrimaryBlue` | Acciones y navegación activa | `#2563EB` |
| `StatusSuccess` | Al día | `#16A34A` |
| `StatusWarning` | Próxima | `#D97706` |
| `StatusError` | Vencida / destructivo | `#DC2626` |

Los estados de mantenimiento **nunca se comunican solo con color** (WCAG AA): usa `StatusChip`.

Guía completa para el equipo: [`app/src/main/java/com/example/my_car/GUIA_DISENO.md`](app/src/main/java/com/example/my_car/GUIA_DISENO.md).

---

## Estado actual y próximos pasos

**Listo**

- Capas domain / data / UI y Hilt
- Navegación tipada
- Historial, exportación CSV y borrado de datos
- `AlertScheduler` + `MaintenanceAlertWorker`
- Tema Material 3 (claro/oscuro) y `StatusChip`
- CI con tests unitarios

**Pendiente de integración**

- Conectar Room (`VehicleEntity`, `MaintenanceEntity`) y registrar DAOs en `AppDatabase`
- Sustituir los repositorios fake por implementaciones reales
- Implementar `DashboardScreen` (el `DashboardViewModel` ya existe)
- Pantallas de vehículos y plan de mantenimiento
- Compilador de Room (KSP) cuando se activen las entidades en la base de datos

---

## Contribución

El trabajo se integra contra `develop` y `main`.

1. Crea una rama desde `develop`.
2. Mantén la UI alineada con la [guía de diseño](app/src/main/java/com/example/my_car/GUIA_DISENO.md).
3. Incluye o actualiza pruebas unitarias de la lógica que toques.
4. Abre un pull request hacia `develop`. El CI debe pasar antes de fusionar.

---

## Licencia

Uso académico / proyecto de equipo. Aún no hay licencia open source publicada.
