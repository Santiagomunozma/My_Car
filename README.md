# My Car (MiCarro)

[![Android CI](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml/badge.svg)](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml)

Aplicación nativa de Android para el seguimiento de vehículos: kilometraje, historial de mantenimientos, exportación de datos y alertas preventivas.

> **Nota:** el proyecto está en desarrollo activo. El módulo de vehículos, kilometraje y documentos (Compañero 1) ya está integrado con Room.

---

## Características

| Módulo | Estado | Descripción |
| --- | --- | --- |
| Dashboard | Disponible | Vehículo principal y conteo de alertas documentales |
| Vehículos | Disponible | Alta, edición, foto, archivar/reactivar, vehículo principal único |
| Kilometraje | Disponible | Registro de lecturas con confirmación de lecturas menores |
| Documentos | Disponible | SOAT, técnico-mecánica, seguro y otros con estado calculado |
| Alertas documentales | Disponible | Bandeja de vencimientos, switch global/por documento, anticipación 1–180 días |
| Historial | Disponible | Búsqueda de servicios/talleres y listado de mantenimientos |
| Exportación CSV | Disponible | Comparte el historial con `FileProvider` |
| Configuración | Disponible | Borrado irreversible de todos los datos locales |
| Alertas (notificaciones) | Infraestructura lista | Notificaciones programadas con WorkManager |
| Plan de mantenimiento | Disponible | Planificación y registro de servicios (Compañero 2) |

Tipos de vehículo soportados en el modelo de dominio: **automóvil**, **camión** y **motocicleta**.

---

## Stack tecnológico

| Área | Tecnología | Versión |
| --- | --- | --- |
| Lenguaje | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 | BOM 2026.02.01 |
| Arquitectura | Clean Architecture + MVVM | — |
| DI | Hilt | 2.60.1 |
| Persistencia | Room (con KSP) | 2.7.2 |
| Imágenes | Coil | 2.7.0 |
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
- **Domain:** reglas de negocio en objetos `*Rules` y casos de uso puros (`SaveVehicleUseCase`, `AddMileageReadingUseCase`, `ObserveDocumentAlertsUseCase`, `GetDashboardSummaryUseCase`, `ExportHistoryToCsvUseCase`, `ClearAllDataUseCase`) y contratos de repositorio.
- **Data:** vehículos, kilometraje y documentos persisten en Room (`my_car_db`) vía `VehicleRepositoryImpl`, `MileageRepositoryImpl` y `DocumentRepositoryImpl` dentro de cada `feature/*/data`. Mantenimiento y repuestos siguen con implementaciones `Fake*Impl` hasta que el Compañero 2 registre sus entidades.
- **Core:** navegación, DI, Room, WorkManager, `SharedPreferencesAlertSettingsRepository`, tema y utilidades compartidas.

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
| `dashboard_screen` | Inicio |
| `vehicle_list_screen` | Lista de vehículos (con `BadgedBox` de alertas) |
| `add_vehicle_screen?vehicleId=` | Alta / edición de vehículo |
| `mileage_screen/{vehicleId}` | Kilometraje de un vehículo |
| `documents_screen/{vehicleId}` | Documentos de un vehículo |
| `documents/alerts` | Bandeja de alertas documentales |
| `maintenance_plan_screen` | Plan de mantenimiento |
| `maintenance_form_screen/{vehicleId}` | Alta de plan |
| `service_form_screen/{vehicleId}?planId=&lastMileage=` | Registro de servicio |
| `history_screen` | Historial |
| `settings_screen` | Configuración |

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

Pruebas unitarias actuales (62 en total):

- `GetDashboardSummaryUseCaseTest`, `HistoryViewModelTest`, `MaintenanceRulesTest` — base del equipo
- `VehicleRulesTest` — normalización de placa, año, kilometraje, VIN, cilindraje
- `VehicleUseCasesTest` — alta/edición, placa duplicada, archivar/reactivar, vehículo principal único, foto (guardar/quitar/reemplazar), vehículo desaparecido
- `MileageUseCasesTest` — lectura mayor/menor/confirmada, fecha futura, entradas inválidas, actualización del odómetro y notificador
- `DocumentStatusRulesTest` — estados documentales y límites de anticipación (vence hoy, límite exacto, cambio de día)
- `DocumentUseCasesTest` — bandeja de alertas (identifica vehículo/documento/causa, archivados, switches) y alta/eliminación
- `VehicleFormViewModelTest` — doble guardado bloqueado y fallo de importación de foto

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
- Navegación tipada con bottom bar (Inicio, Vehículos, Mantenimiento, Historial, Ajustes)
- **Compañero 1:** vehículos (RF-01…RF-05), kilometraje (RF-06…RF-09) y documentos (RF-32…RF-33) con Room real
- Fotografía de vehículo con `PickVisualMedia` + `VehiclePhotoStore` (sin permisos)
- Bandeja de alertas documentales con `AlertSettingsRepository` (SharedPreferences)
- Historial, exportación CSV y borrado de datos
- `AlertScheduler` + `MaintenanceAlertWorker`
- Tema Material 3 con la paleta oficial y `StatusChip`/`MiCarroCard`/botones/`MiCarroTextField`/`DateField` compartidos
- CI con tests unitarios

**Pendiente de integración**

- Registrar las entidades de mantenimiento/repuestos en `AppDatabase` y enlazar sus repositorios Room (Compañero 2)
- Implementar `MileageAlertNotifier` real (hoy `NoOpMileageAlertNotifier`) para recalcular alertas por km tras cada lectura (RF-09)
- Notificaciones push del sistema para vencimientos documentales (módulo de alertas)
- Pruebas de emulador/UI (`connectedAndroidTest`) y de release

**Puntos de integración para el orquestador**

- `domain/alerts/MileageAlertNotifier` — contrato `suspend fun onMileageUpdated(vehicleId, odometer)`; el binding NoOp está en `core/di/RepositoryModule.kt`.
- `AppDatabase` (`core/database`) — registrar aquí las entidades de otros módulos; ya incluye `VehicleEntity`, `MileageEntity`, `DocumentEntity` (v1).
- `domain/repository/AlertSettingsRepository` — configuración compartida de alertas (switch global + anticipación en días); implementación en `core/settings`.
- `VehicleRepository.observeAllVehicles()` — incluye archivados (usado por la bandeja de alertas); `observeVehicles()` sigue devolviendo solo activos.

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
