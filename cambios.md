# Registro de cambios — MiCarro

Documento de seguimiento del avance del proyecto. Cada sesión de trabajo agrega
su entrada al final (o actualiza el estado de pendientes).

---

## 2026-09-17 — Primer avance: módulo Compañero 1 (Vehículos, Kilometraje y Documentos)

Commit: `eaa88ea` en rama `Marin` (basada en `develop` @ `ff334f9`).

### Verificación

| Comando | Resultado |
| --- | --- |
| `./gradlew assembleDebug` | OK |
| `./gradlew testDebugUnitTest` | 62/62 tests |
| `./gradlew lintDebug` | 0 errores |

### Hecho

**Vehículos (RF-01…RF-05)**
- Modelo `Vehicle` extendido: `model`, `color`, `vin`, `fuelType`, `engineCc`, `photoUri` (campos nuevos con defaults, sin romper el contrato compartido).
- Alta/edición con validación en dominio (`VehicleRules` + `SaveVehicleUseCase`): placa 3–10 alfanumérica/guiones normalizada (mayúsculas, sin espacios) y única, año 1900–año+1, kilometraje entero ≥ 0, VIN 17 chars si se provee, cilindraje > 0 si se provee.
- Archivar/reactivar con diálogo de confirmación; el historial se conserva.
- Un solo vehículo principal (`VehicleDao.setMainVehicle` transaccional).
- Foto con `PickVisualMedia` (sin permisos) → `VehiclePhotoStore` (interfaz en domain) / `LocalVehiclePhotoStore` (copia a `files/vehicle_photos/`); al guardar se borra el archivo gestionado si cambió o se quitó; fallo de importación visible.
- Edición segura: recarga el estado del repositorio y conserva `currentMileage`, `isArchived`, `isMainVehicle`; odómetro de solo lectura con texto de ayuda; vehículo desaparecido → `saveFailed`.
- Lista con filtro Activos/Archivados, `FlowRow` de acciones, miniatura 56dp (Coil), badges Principal/Archivado, FAB con espacio inferior, `BadgedBox` con conteo de alertas documentales.

**Kilometraje (RF-06…RF-09)**
- `MileageRecord` extendido con `note`.
- `AddMileageReadingUseCase`: rechaza vacío/decimal/no convertible/negativo (`toLongOrNull`, nunca a 0) y fechas futuras; lectura menor → `RequiresConfirmation` (la UI exige confirmar o corregir; cambiar odómetro o fecha descarta la confirmación).
- Tras guardar: actualiza `Vehicle.currentMileage` según la última lectura cronológica (fecha, empate por id) y llama a `MileageAlertNotifier` usando el estado fresco del repo.
- Historial completo ordenado desc; no se borran lecturas.
- Contrato `domain/alerts/MileageAlertNotifier` + `NoOpMileageAlertNotifier` enlazado en Hilt hasta que llegue la implementación del módulo de alertas.

**Documentos (RF-32…RF-33)**
- Modelo `VehicleDocument` + `DocumentType` (SOAT/TECHNICAL_INSPECTION/INSURANCE/OTHER) + `DocumentStatus`.
- Alta/edición/eliminación con confirmación; nombre obligatorio solo para OTHER; diálogo desplazable (`verticalScroll`); `DateField` accesible con `DatePickerDialog` de Material 3.
- `DocumentStatusRules`: EXPIRED/UPCOMING/UP_TO_DATE calculado en dominio; se recalcula al cambiar el día (combine docs + settings + ticker de 60 s con `distinctUntilChanged`).
- `AlertSettingsRepository` (SharedPreferences): switch global + anticipación 1–180 días (defecto 30).
- Bandeja `documents/alerts`: identifica placa, documento y causa ("Faltan N días"/"Vencido hace N días"/"Vence hoy" con plurals); incluye vehículos archivados; respeta switch global y por documento; tap navega a los documentos del vehículo; vencidos primero.

**Infraestructura / integración**
- `AppDatabase` v1 con `VehicleEntity`, `MileageEntity`, `DocumentEntity` + DAOs en `DatabaseModule`.
- `RepositoryModule`: binds reales de vehículos/kilometraje/documentos, `VehiclePhotoStore`, `AlertSettingsRepository`, `MileageAlertNotifier` (NoOp). Fakes de mantenimiento/partes se conservan.
- Navegación: rutas `add_vehicle_screen?vehicleId=`, `mileage_screen/{id}`, `documents_screen/{id}`, `documents/alerts`, más `maintenance_form_screen/{id}` y `service_form_screen/{id}?planId=&lastMileage=` del Compañero 2 cableadas.
- `MainActivity`: se eliminó el `TestDevPanel`; shell real con `NavigationBar` (Inicio, Vehículos, Mantenimiento, Historial, Ajustes) + `NavHost`; permiso `POST_NOTIFICATIONS` se pide al arranque.
- Componentes nuevos en `ui/components`: `MiCarroCard`, `PrimaryButton`, `SecondaryButton`, `DestructiveButton`, `MiCarroTextField`, `DateField`, `EmptyState`.
- `Theme.kt` corregido: antes usaba `#0061A4` + dynamic color (pisaba la paleta oficial) y no pasaba `AppShapes`; ahora usa los tokens oficiales.
- `strings.xml` completo con plurals; sin textos hardcodeados en pantallas nuevas (también se quitó el literal "Sin vehículos registrados" de `DashboardViewModel`).
- `DashboardScreen` mínima conectada al `DashboardViewModel` existente.
- `FakeVehicleRepositoryImpl` actualizado al contrato ampliado (lo usan tests existentes); entity vieja `data/local/entity/VehicleEntity` eliminada.
- `ClearAllDataUseCase` (`clearAllTables`) cubre las nuevas tablas automáticamente.

### Cambios de dependencias

- `ksp` `2.0.21-1.0.27` → `2.2.10-2.0.2` (debe coincidir con Kotlin 2.2.10).
- `room` `2.6.1` → `2.7.2` (2.6.1 falla con KSP2: `unexpected jvm signature V`).
- Nuevas: `coil-compose:2.7.0`, `lifecycle-runtime-compose:2.11.0`, `desugar_jdk_libs:2.1.5` (`isCoreLibraryDesugaringEnabled`), `room-compiler` vía KSP habilitado.

### Decisiones tomadas (adaptaciones al repo compartido)

- `VehicleType` conserva `TRUCK` (no `PICKUP` del prompt): el contrato ya estaba integrado; la UI lo etiqueta "Camioneta".
- IDs `String` (UUID) y odómetro `Int`: contratos existentes, no se migraron.
- `VehicleRepository` ganó métodos aditivos (`observeAllVehicles`, `setMainVehicle`, `updateCurrentMileage`); firmas previas intactas.
- Entidades/DAOs de mantenimiento y repuestos existen en el código pero NO se registraron en `AppDatabase` (le toca al Compañero 2); sus repositorios siguen en fakes.

### Tests nuevos

- `VehicleRulesTest` (6), `VehicleUseCasesTest` (15), `MileageUseCasesTest` (8), `DocumentStatusRulesTest` (5), `DocumentUseCasesTest` (8), `VehicleFormViewModelTest` (4) + `fakes/` en memoria + `MainDispatcherRule`.

### Pendiente

- `MileageAlertNotifier` real (motor de alertas por km) — módulo del Compañero 2/3.
- Registrar entidades de mantenimiento/repuestos en `AppDatabase` y sus repos Room — Compañero 2.
- Notificaciones push del sistema para vencimientos documentales — módulo de alertas.
- Pruebas de emulador/UI (`connectedAndroidTest`) y de release.
- Warnings restantes (deprecaciones en código del equipo: `ArrowBack`, `Divider`, `ScrollableTabRow`).

---

## 2026-09-22 — Pendientes de integración vs guía Compañero 1c

Revisión contra el archivo `# MiCarro — Sistema móvil para el control del mantenimiento vehicular`.

### Diferencias pendientes (por integrar)

- [x] Renombrar paquete `com.example.my_car` a `com.micarro` y `applicationId` a `com.micarro`.
- [x] Cambiar `Vehicle.id` de `String` a `Long` (guía: `Long`).
- [x] Cambiar `Vehicle.currentMileage` de `Int` a `Long`.
- [ ] Renombrar `Vehicle.isMainVehicle` a `Vehicle.isPrimary`.
- [ ] Cambiar `MileageRecord` (`id`, `vehicleId`) de `String` a `Long`.
- [ ] Cambiar `MileageRecord.date` de `Long` (epoch millis) a `LocalDate`.
- [ ] Cambiar `MileageRecord.reading` de `Int` a `Long`.
- [ ] Renombrar `MileageRecord` a `MileageReading` (nombre de la guía).
- [ ] Cambiar `VehicleDocument.id` y `vehicleId` de `String` a `Long`.
- [ ] Cambiar `VehicleDocument.expirationDate` de `Long` a `LocalDate`.
- [ ] Alinear versiones del stack: Kotlin 2.0.x, Room 2.6.1, KSP compatible (actualmente Kotlin 2.2.10, Room 2.7.2).
- [ ] Borrar fakes muertos: `FakeVehicleRepositoryImpl`, `FakeMileageRepositoryImpl`, `FakeMaintenanceRepositoryImpl`, `FakePartRepositoryImpl` (eliminados del código base).
- [ ] Verificar/conectar `MileageAlertNotifier` a la implementación real del módulo de alertas (actualmente `NoOp`).
- [ ] Validar con el orquestador los cambios hechos en archivos centrales: `AppDatabase`, `MainActivity`, `NavGraph`, `RepositoryModule`, `DatabaseModule`.

### Funcionalidad ya cubierta (sin pendientes)

- RF-01 a RF-05 (vehículos) implementados.
- RF-06 a RF-09 (kilometraje) implementados.
- RF-32 a RF-33 (documentos y alertas documentales) implementados.
- Tests mínimos de la guía cubiertos por `VehicleUseCasesTest`, `MileageUseCasesTest`, `DocumentUseCasesTest` y `VehicleFormViewModelTest`.
