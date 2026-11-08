# My Car

[![Android CI](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml/badge.svg)](https://github.com/Santiagomunozma/My_Car/actions/workflows/android_ci.yml)

Aplicación nativa de Android para el dueño de un vehículo en Colombia. Lleva el kilometraje, el plan de mantenimiento (por fecha y por kilómetros), los documentos que vencen, el historial de gastos y un asistente que responde con ese historial. Los datos quedan en el teléfono: no hay cuenta ni servidor propio.

El nombre de producto es **My Car**, en el icono y dentro de la app. El paquete es `com.micarro` y la versión es **1.0** (`versionCode` 1). No está en Play Store.

> **Nota:** el proyecto está en desarrollo activo. Vehículos, kilometraje, documentos, planes, servicios y repuestos persisten en Room. La UI sigue el sistema de diseño **Aqua** (cian `#38BDF8` → índigo `#818CF8`). Leer un documento o usar el asistente llama a Gemini y exige `GEMINI_API_KEY` en `local.properties`; además hace falta Premium o la prueba de 30 días. El banner de la versión gratuita usa los anuncios de prueba de Google y pide internet. El resto funciona sin internet.

---

## Características

| Módulo | Estado | Descripción |
| --- | --- | --- |
| Inicio | Disponible | Héroe del vehículo activo, próximo mantenimiento, accesos rápidos y servicios recientes |
| Vehículos | Disponible | Alta, edición, foto, archivar/reactivar, vehículo principal único |
| Kilometraje | Disponible | Registro de lecturas con confirmación de lecturas menores |
| Documentos | Disponible | SOAT, técnico-mecánica, seguro, impuesto, semaforización y otros. Estado vigente, por vencer o vencido. Los datos se escriben a mano en la versión gratuita. Adjuntar foto o PDF (hasta 8 MB), y que Gemini los lea, es de Premium o de la prueba |
| Alertas documentales | Disponible | Bandeja de vencimientos, switch global y por documento. El estado «por vencer» usa una anticipación de 1–180 días. El aviso local sale a los 30 días, a los 14 y el día del vencimiento |
| Plan de mantenimiento | Disponible | Actividades por km y por meses, plantillas, servicios preventivos o correctivos, repuestos con garantía |
| Historial | Disponible | Búsqueda, filtros en hoja, resumen de gastos (mes, 6 meses, año o todo) |
| Exportación | Disponible | CSV del historial en la versión gratuita. Excel (xlsx) y PDF, y la copia de seguridad, son de Premium o de la prueba |
| Asistente | Premium | Chat con Gemini 2.5 Flash usando los servicios y repuestos del vehículo. Orientación, no diagnóstico. Abre Google Maps con la búsqueda «taller mecánico». Se abre con Premium o con la prueba |
| Premium | Simulado | Pago único de $20.000 o prueba de 30 días. Quita el banner, y abre asistente, fotos de documentos, Excel, PDF y copia de seguridad. El cobro no sale del teléfono |
| Ajustes | Disponible | Tema claro/sistema/oscuro, datos de ensayo para la ponencia y borrado irreversible. Borrar datos no quita Premium |
| Alertas (notificaciones) | Disponible | WorkManager avisa en el teléfono por documentos, mantenimiento y garantía de repuestos. Al registrar un kilometraje se reprograman las actividades de ese vehículo |

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
| Asistente y lectura | API de Gemini (`gemini-2.5-flash`) | clave local, no va en el repo |
| Anuncios | Google Mobile Ads, ids de prueba | no van a Play Store |
| Build | Android Gradle Plugin / Gradle | 9.3.3 / 9.5.0 |
| JDK | Java / toolchain Gradle | 17 |
| SDK | `minSdk` 26 · `targetSdk` / `compileSdk` 37 | — |

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
- **Data:** vehículos, kilometraje, documentos, mantenimiento y repuestos en Room (`my_car_db`, esquema 5). Las fotos y los soportes quedan en el almacenamiento de la app. `GeminiDocumentReader` y `GeminiAssistantClient` solo se usan al leer un archivo o al preguntar en el asistente.
- **Core:** navegación, DI, Room, WorkManager, `ThemeManager`, workers y utilidades compartidas.

---

## Estructura del proyecto

```
My_Car/
├── .github/workflows/             # CI de tests y APK de ponencia
├── CONTEXTO_PONENCIA.md           # Briefing para armar la presentación
├── DESIGN_SYSTEM.md               # Tokens Aqua, componentes y reglas de densidad
├── GLOSARIO_UI.md                 # Terminología y voz (tuteo, sentence case)
├── MAQUETA_AQUA.md                # Antes/después del rediseño limpio
├── AUDITORIA_VISUAL.md            # Auditoría previa al rediseño
├── presentar.bat / presentar.ps1  # APK, QR en la red local y espejo del celular
├── ponencia/                      # Página de descarga y keystore de demostración
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/micarro/
│       │   ├── core/              # DI, Room, navegación, workers, settings
│       │   ├── domain/            # modelos, repositorios, casos de uso
│       │   ├── feature/           # dashboard, vehicle, documents, maintenance, history, assistant…
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
| Asistente, Ajustes, Alertas documentales, Alertas de mantenimiento, Copia de seguridad | Hoja **Más** |
| Formularios (vehículo, actividad, servicio, km, documentos) | Stack de detalle con atrás |

Rutas (`Screen`): `dashboard_screen`, `vehicle_list_screen`, `add_vehicle_screen`, `mileage_screen/{vehicleId}`, `documents_screen/{vehicleId}`, `documents/alerts`, `maintenance_plan_screen`, `maintenance_form_screen`, `service_form_screen`, `alert_settings_screen`, `history_screen`, `settings_screen`, `backup_screen`, `assistant_screen`, `premium_screen`.

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
- Emulador o dispositivo con Android 8.0 (API 26) o superior

Para el asistente y la lectura de documentos, agrega en `local.properties` (no se commitea):

```properties
GEMINI_API_KEY=tu_clave
```

Sin esa clave la app abre igual. El archivo se guarda y el chat muestra que falta la clave.

### Windows

Clona el proyecto en una ruta **sin espacios ni caracteres especiales** (por ejemplo `C:\Proyecto\My_Car`).

Si la caché de Gradle en el perfil de usuario da problemas:

```powershell
.\gradlew.bat test -g C:\gradle_home
```

---

## Ponencia

En el salón, con el computador conectado al proyector:

```powershell
.\presentar.bat
```

Eso hace tres cosas:

1. Compila un APK firmado (solo la primera vez, o de nuevo con `.\presentar.bat -Compilar`).
2. Abre una página en la red local con un código QR. El público, en el **mismo Wi-Fi**, escanea, descarga `MiCarro.apk` y permite instalar desde el navegador.
3. Si el celular está conectado por USB con depuración activada, lo instala si aún no tiene la app y abre su pantalla en una ventana (scrcpy) para proyectarla. La primera vez instala scrcpy.

Deja abierta la ventana de la consola mientras quieras que el QR siga funcionando. `Ctrl+C` apaga solo la descarga.

La firma del APK es de demostración (`ponencia/micarro-ponencia.keystore`). Sirve para instalar a mano. No sirve para Play Store. Si el teléfono ya tiene la versión de Android Studio, el script no la reemplaza, para no borrar los datos de la demo.

También puedes generar el APK en GitHub: Actions → **APK ponencia** → Run workflow, y descargar el artefacto `MiCarro`.

Antes de proyectar, en el teléfono: **Ajustes → Cargar datos de ensayo**. Reemplaza lo que haya en ese dispositivo por un historial fijo (Onix `KHM452`, Yamaha FZ y un Logan archivado) con fechas relativas al día de la carga, para que el aceite siga vencido y la técnico-mecánica siga próxima. El relato, el público y el material de la presentación están en [`CONTEXTO_PONENCIA.md`](CONTEXTO_PONENCIA.md).

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

- Dashboard, historial (incluido Excel), reglas de mantenimiento
- Vehículos, kilometraje, placas colombianas y catálogo de marcas
- Documentos, lectura del archivo y recordatorios a 30 días, 14 días y vencimiento
- Escenario de ensayo de la ponencia y casos de uso de dominio

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
- Navegación de 4 pestañas + hoja **Más** (asistente, ajustes, alertas, copia de seguridad)
- Rediseño visual Aqua: densidades bajas, ⋮, hojas inferiores y tiles de acceso
- Vehículos, kilometraje, documentos y mantenimiento con Room
- Lectura de documentos y asistente con Gemini, si hay clave y si hay Premium o la prueba de 30 días
- Premium simulado: pago único de $20.000 guardado en el teléfono, prueba de un mes, banner de prueba de Google en la versión gratuita
- Historial con filtros en hoja; exportación Excel / PDF / CSV
- Copia de seguridad y datos de ensayo para la ponencia
- Tema claro / sistema / oscuro persistido
- Workers de mantenimiento, documentos y garantía. `MileageAlertScheduler` reprograma las actividades al cambiar el odómetro
- CI con tests unitarios

**Pendiente**

- Publicar en Play Store (la firma actual es solo de demostración)
- Sincronizar entre teléfonos. Hoy el cambio de aparato es restaurar la copia
- Pruebas de UI en emulador (`connectedAndroidTest`) como parte del CI

**Puntos de integración**

- `domain/alerts/MileageAlertNotifier` — lo implementa `feature/alerts/domain/MileageAlertScheduler`; el binding está en `core/di/RepositoryModule.kt`
- `AppDatabase` (`core/database`) — registro de entidades
- `domain/repository/AlertSettingsRepository` — switch global y anticipación; `core/settings`
- `VehicleRepository.observeAllVehicles()` — incluye archivados (bandeja de alertas)
- `GEMINI_API_KEY` en `local.properties` — la leen `GeminiDocumentReader` y `GeminiAssistantClient`

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
