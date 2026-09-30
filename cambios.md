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
- [x] Renombrar `Vehicle.isMainVehicle` a `Vehicle.isPrimary`.
- [x] Cambiar `MileageRecord` (`id`, `vehicleId`) de `String` a `Long`.
- [x] Cambiar `MileageRecord.date` de `Long` (epoch millis) a `LocalDate`.
- [x] Cambiar `MileageRecord.reading` de `Int` a `Long`.
- [x] Renombrar `MileageRecord` a `MileageReading` (nombre de la guía).
- [x] Cambiar `VehicleDocument.id` y `vehicleId` de `String` a `Long`.
- [x] Cambiar `VehicleDocument.expirationDate` de `Long` a `LocalDate`.
- [ ] Alinear versiones del stack: Kotlin 2.0.x, Room 2.6.1, KSP compatible (actualmente Kotlin 2.2.10, Room 2.7.2).
- [x] Borrar fakes muertos: `FakeVehicleRepositoryImpl`, `FakeMileageRepositoryImpl`, `FakeMaintenanceRepositoryImpl`, `FakePartRepositoryImpl` (eliminados del código base).
- [x] Verificar/conectar `MileageAlertNotifier` a la implementación real del módulo de alertas (actualmente `NoOp`).
- [x] Validar con el orquestador los cambios hechos en archivos centrales: `AppDatabase`, `MainActivity`, `NavGraph`, `RepositoryModule`, `DatabaseModule`.

### Funcionalidad ya cubierta (sin pendientes)

- RF-01 a RF-05 (vehículos) implementados.
- RF-06 a RF-09 (kilometraje) implementados.
- RF-32 a RF-33 (documentos y alertas documentales) implementados.
- Tests mínimos de la guía cubiertos por `VehicleUseCasesTest`, `MileageUseCasesTest`, `DocumentUseCasesTest` y `VehicleFormViewModelTest`.

---

## 2026-09-29 — Avance Compañero 2: Plan de mantenimiento, Servicios, Repuestos y Alertas

Rama: `Herrera` (basada en `develop`).

### Verificación

| Comando | Resultado |
| --- | --- |
| `./gradlew assembleDebug` | OK |
| `./gradlew testDebugUnitTest` | 66/66 tests |
| `./gradlew lintDebug` | 0 errores |

### Hecho

**Plan de mantenimiento (RF-10…RF-16)**
- Modelo `MaintenancePlan` extendido con `isActive` para pausar/reactivar actividades.
- `MaintenanceRules`: implementación completa de RN-01 a RN-08 (estados vencida/próxima/al día/sin programación, cálculo de recurrencia, validación de fechas/kilometraje).
- Alta/edición de planes con plantillas rápidas (RF-16): Cambio de Aceite, Filtros, Frenos, Batería.
- Programación por fecha (meses) y/o kilometraje con anticipación configurable.
- Estados visuales con `StatusChip`: Vencida (rojo), Próxima (amarillo), Al día (verde), Pausado.
- Eliminar solo si no tiene historial asociado (RF-15) con diálogo de confirmación y rechazo informativo.
- Pausar/reactivar actividades sin afectar historial.

**Servicios realizados (RF-17…RF-23)**
- Registro de servicios preventivos/correctivos con selector de tipo (RF-17).
- Campos: vehículo, fecha de realización, kilometraje, tipo, título, taller/responsable, mano de obra, repuestos.
- RN-05: Validación de fecha futura rechazada con error visual.
- RN-06: Kilometraje menor al último con advertencia visual y diálogo de confirmación obligatorio.
- Cálculo automático de costo total (RN-04): mano de obra + repuestos.
- Al completar servicio periódico, cálculo automático de siguiente recurrencia (RN-03).
- Integración con `DateField` para selección de fecha.

**Repuestos (RF-24…RF-27)**
- Modelo `Part` extendido con campos opcionales: `brand`, `reference`, `provider`, `installationDate`, `warranty`, `notes`.
- Formulario de repuestos con campos de marca y referencia (RF-24).
- Asociar múltiples repuestos a un servicio de mantenimiento.
- Visualización de repuestos actualmente instalados por vehículo (RF-27) con pestaña dedicada.
- Cálculo de costo total por repuesto (cantidad × valor unitario).

**Alertas locales (RF-28…RF-31)**
- `AlertScheduler` con contratos del equipo: `scheduleForActivity`, `cancelForActivity`, `postpone`.
- RN-08: Posponer mueve el aviso, no el vencimiento (implementado en `snoozeAlert`).
- `AlertCalculator`: lógica pura para evaluar si debe lanzar alerta inmediata.
- Configuración de anticipación (días y kilómetros) en `AlertSettingsScreen`.
- Programación de alertas al crear/actualizar planes en `MaintenanceViewModel`.
- Cancelación de alertas al eliminar planes.

**Infraestructura / integración**
- `AppDatabase` v2: registro de `MaintenancePlanEntity`, `MaintenanceServiceEntity`, `PartEntity` con sus DAOs.
- `DatabaseModule`: provisión de `MaintenancePlanDao`, `MaintenanceServiceDao`, `PartDao`.
- `RepositoryModule`: binding de `MaintenanceRepositoryImpl` y `PartRepositoryImpl`.
- `MaintenanceRepository`: contrato extendido con `updatePlanActiveStatus`.
- `MaintenancePlanDao`: query `updateActiveStatus` para pausar/reactivar sin afectar historial.
- `MaintenanceServiceDao`: queries agregadas para historial con filtros y gastos por categoría.
- `PartDao`: query `observeInstalledParts` con JOIN a servicios para filtrar por vehículo.
- Navegación: rutas `maintenance_plan_screen`, `alert_settings_screen`, `maintenance_form_screen/{vehicleId}`, `service_form_screen/{vehicleId}?planId=&lastMileage=`.
- `AlertScheduler` (core): refactorizado para soportar inyección en tests (constructor secundario sin contexto).
- `MaintenancePlanScreen`: pestañas Planes/Repuestos instalados, selector de vehículos, FAB condicional.
- `MaintenanceFormScreen`: plantillas rápidas, corrección de `vehicleId` (usar ID en lugar de placa).
- `ServiceFormScreen`: validaciones RN-05/RN-06, selector tipo preventivo/correctivo, diálogo de confirmación.

**Modelos de dominio actualizados**
- `MaintenancePlan`: agregado `isActive: Boolean = true`.
- `Part`: agregados campos opcionales `brand`, `reference`, `provider`, `installationDate`, `warranty`, `notes`.
- `MaintenanceService`: mantiene estructura existente con `planId` nullable para servicios correctivos.

**Tests nuevos**
- `MaintenanceUseCasesTest` (4): eliminar sin/con historial, múltiples repuestos, pausar/reactivar.
- `AlertSchedulerTest` (4): programar alerta, cancelar, posponer (RN-08), snooze.
- `MaintenanceRulesTest` (9): estados vencida/próxima/al día, recurrencia, costo total, validaciones RN-05/RN-06, RN-08.
- `InMemoryMaintenanceRepository` y `InMemoryPartRepository` en `fakes/InMemoryFakes.kt` para testing.

### Reglas de negocio implementadas (RN-01 a RN-08)

| Regla | Descripción | Implementación |
| --- | --- | --- |
| **RN-01** | Vencida si supera fecha o kilometraje límite | `MaintenanceRules.calculateStatus` |
| **RN-02** | Próxima si entra en margen de anticipación | `MaintenanceRules.calculateStatus` |
| **RN-03** | Siguiente recurrencia nace del servicio real | `MaintenanceRules.calculateNextRecurrence` |
| **RN-04** | Total = mano de obra + repuestos + otros | `MaintenanceRules.calculateTotalCost` |
| **RN-05** | No permitir fecha futura como realización | `MaintenanceRules.isDateValid` + validación UI |
| **RN-06** | Kilometraje menor exige advertencia/confirmación | `MaintenanceRules.isMileageValid` + diálogo UI |
| **RN-08** | Posponer mueve aviso, no vencimiento | `AlertScheduler.postpone` + `snoozeAlert` |

### RFs cubiertos (RF-10 a RF-31)

- **RF-10…RF-16**: Plan de mantenimiento completo ✓
- **RF-17…RF-23**: Registro de servicios con validaciones ✓
- **RF-24…RF-27**: Repuestos con campos extendidos y trazabilidad ✓
- **RF-28…RF-31**: Alertas locales con configuración y programación ✓

### Pendiente

- Integración con notificaciones push del sistema (módulo de alertas avanzado).
- Pruebas de emulador/UI (`connectedAndroidTest`) y de release.
- Corrección de warnings de deprecación (`menuAnchor`, `fallbackToDestructiveMigration`).
