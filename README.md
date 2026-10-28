# Mi Carro

[![Android CI](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml/badge.svg)](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml)

Aplicación nativa de Android para el seguimiento de vehículos: kilometraje, plan de mantenimiento, documentos, historial, exportación y alertas preventivas.

> **Nota:** el proyecto está en desarrollo activo. Vehículos, kilometraje, documentos y mantenimiento persisten en Room. La UI sigue el sistema de diseño **Aqua** (cian `#38BDF8` → índigo `#818CF8`).

---

## Características

| Módulo | Estado | Descripción |
| --- | --- | --- |
| Inicio | Disponible | Héroe del vehículo activo, próximo mantenimiento, accesos rápidos y servicios recientes |
| Vehículos | Disponible | Alta, edición, foto, archivar/reactivar, vehículo principal único |
| Kilometraje | Disponible | Registro de lecturas con confirmación de lecturas menores |
| Documentos | Disponible | SOAT, técnico-mecánica, seguro y otros con estado calculado |
| Alertas documentales | Disponible | Bandeja de vencimientos, switch global/por documento, anticipación 1–180 días |
| Plan de mantenimiento | Disponible | Actividades, registro de servicios, repuestos instalados y alertas |
| Historial | Disponible | Búsqueda, filtros en hoja, resumen de gastos |
| Exportación | Disponible | Excel (xlsx), PDF y CSV del historial |
| Ajustes | Disponible | Tema claro/sistema/oscuro, copia de seguridad y borrado irreversible |
| Alertas (notificaciones) | Infraestructura lista | Notificaciones programadas con WorkManager |

Tipos de vehículo en el dominio: **automóvil**, **camioneta** y **motocicleta**.

---

## Stack tecnológico

| Área | Tecnología | Versión |
| --- | --- | --- |
| Lenguaje | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 | BOM 2026.02.01 |
| Arquitectura | Clean Architecture + MVVM | — |
| DI | Hilt | 2.60.1 |
| Persistencia | Room (con KSP) | 2.7.2 |
| Exportación | Apache POI (Excel) | 5.2.5 |
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
└─────────────────────────────────────────┘
```

- **UI:** pantallas Compose, `UiState` inmutable y ViewModels con Hilt (`@HiltViewModel`).
- **Domain:** reglas de negocio en objetos `*Rules` y casos de uso puros; contratos de repositorio.
- **Data:** vehículos, kilometraje, documentos, mantenimiento y repuestos en Room (`my_car_db`).
- **Core:** navegación, DI, Room, WorkManager, `ThemeManager`, workers y utilidades compartidas.

---

## Estructura del proyecto

```
My_Car/
├── .github/workflows/android_ci.yml
├── DESIGN_SYSTEM.md               # Tokens Aqua, componentes y reglas de densidad
├── GLOSARIO_UI.md                 # Terminología y voz (tuteo, sentence case)
├── MAQUETA_AQUA.md                # Antes/después del rediseño limpio
├── AUDITORIA_VISUAL.md            # Auditoría previa al rediseño
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/micarro/
│       │   ├── core/              # DI, Room, navegación, workers, settings
│       │   ├── domain/            # modelos, repositorios, casos de uso
│       │   ├── feature/           # dashboard, vehicle, maintenance, history…
│       │   └── ui/                # tema Aqua, componentes compartidos
│       ├── test/
│       └── androidTest/
├── gradle/libs.versions.toml
└── gradlew / gradlew.bat
```

### Navegación

Barra inferior flotante (píldora) con **4 pestañas** + **Más** (hoja inferior, no es una ruta):

| Destino | Cómo se abre |
| --- | --- |
| Inicio, Vehículos, Mantenimiento, Historial | Pestañas de la barra |
| Ajustes, Alertas documentales, Alertas de mantenimiento, Respaldo | Hoja **Más** |
| Formularios (vehículo, actividad, servicio, km, documentos) | Stack de detalle con atrás |

Rutas (`Screen`): `dashboard_screen`, `vehicle_list_screen`, `add_vehicle_screen`, `mileage_screen/{vehicleId}`, `documents_screen/{vehicleId}`, `documents/alerts`, `maintenance_plan_screen`, `maintenance_form_screen`, `service_form_screen`, `alert_settings_screen`, `history_screen`, `settings_screen`, `backup_screen`.

En pantallas ≥ 600 dp se usa `NavigationRail` con los mismos destinos.

---

## Diseño UI/UX — Aqua

La identidad sale del logo real: degradado **cian `#38BDF8` → índigo `#818CF8`**. El primario de acción en claro es `#0369A1` (WCAG AA con texto blanco). Tema **sistema / claro / oscuro** persistido sin flash.

| Idea | Cómo se aplica |
| --- | --- |
| Agua | Lavado vertical tenue cian→índigo en fondos; tarjetas suaves sin borde duro en claro |
| Densidad | 1 acción principal visible; máximo 3 acciones en el primer pantallazo; el resto en ⋮ u hoja |
| Componentes | `ActionTile`, `AquaCard`, `MiCarroSheet`, `OverflowMenu`, `GroupedList` / `MiCarroListRow`, `StatusChip`, botones y campos compartidos |
| Tipografía | Plus Jakarta Sans + JetBrains Mono (placa, VIN, km) |
| Forma | Controles 16 dp, tarjetas 20–24 dp, hojas 28 dp |

Regla de equipo: **ningún módulo define colores HEX ni radios propios.** Usa los tokens de `ui/theme` y los componentes de `ui/components`.

Documentación:

- [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md) — tokens y componentes
- [`GLOSARIO_UI.md`](GLOSARIO_UI.md) — términos (vehículo, mantenimiento, actividad, repuesto…)
- [`MAQUETA_AQUA.md`](MAQUETA_AQUA.md) — maqueta del rediseño limpio
- [`AUDITORIA_VISUAL.md`](AUDITORIA_VISUAL.md) — hallazgos previos

---

## Requisitos

- [Android Studio](https://developer.android.com/studio) (última versión estable recomendada)
- JDK **17**
- Emulador o dispositivo con Android 7.0 (API 24) o superior

### Windows

Clona el proyecto en una ruta **sin espacios ni caracteres especiales** (por ejemplo `C:\Proyecto\My_Car`).

Si la caché de Gradle en el perfil de usuario da problemas:

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

El `applicationId` es `com.micarro` y la versión actual es **1.0** (`versionCode` 1).

Para instalar solo cuando el emulador ya muestre `device` en `adb devices` (no `offline`):

```powershell
.\gradlew.bat :app:installDebug
adb shell am start -n com.micarro/.MainActivity
```

---

## Pruebas

```powershell
.\gradlew.bat test
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat connectedAndroidTest
```

Cobertura unitaria actual (entre otras):

- Dashboard, historial (incl. Excel), reglas de mantenimiento
- Vehículos, kilometraje, documentos y alertas documentales
- Formularios y casos de uso de dominio

---

## Integración continua

El workflow [`.github/workflows/android_ci.yml`](.github/workflows/android_ci.yml) corre en cada **push** y **pull request** hacia `develop` y `main`:

1. Checkout
2. JDK 17 (Temurin) con caché de Gradle
3. `./gradlew test --stacktrace`

---

## Estado actual y próximos pasos

**Listo**

- Capas domain / data / UI y Hilt
- Navegación 4 pestañas + hoja **Más** (Ajustes, alertas, respaldo)
- Rediseño visual Aqua: densidades bajas, ⋮, hojas inferiores y tiles de acceso
- Vehículos, kilometraje, documentos y mantenimiento con Room
- Historial con filtros en hoja; exportación Excel / PDF / CSV
- Tema claro / sistema / oscuro persistido
- `AlertScheduler` + workers de mantenimiento, documentos y garantía
- CI con tests unitarios

**Pendiente**

- Notificador real de alertas por km (`MileageAlertNotifier`; hoy hay binding NoOp)
- Notificaciones push de vencimientos documentales en el sistema
- Pruebas de UI en emulador (`connectedAndroidTest`) y de release

**Puntos de integración**

- `domain/alerts/MileageAlertNotifier` — `suspend fun onMileageUpdated(...)`; binding en `core/di/RepositoryModule.kt`
- `AppDatabase` (`core/database`) — registro de entidades
- `domain/repository/AlertSettingsRepository` — switch global y anticipación; `core/settings`
- `VehicleRepository.observeAllVehicles()` — incluye archivados (bandeja de alertas)

---

## Contribución

El trabajo se integra contra `develop` y `main`.

1. Crea una rama desde `develop`.
2. Mantén la UI alineada con [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md) y [`GLOSARIO_UI.md`](GLOSARIO_UI.md).
3. Incluye o actualiza pruebas unitarias de la lógica que toques.
4. Abre un pull request hacia `develop`. El CI debe pasar antes de fusionar.

---

## Licencia

Uso académico / proyecto de equipo. Aún no hay licencia open source publicada.
