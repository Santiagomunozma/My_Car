# Directrices de Implementación Faltante — Compañero 2
## Módulo: Plan de mantenimiento + Servicios + Repuestos + Alertas

> **Nota importante**: Este documento contiene la guía paso a paso detallada con las modificaciones, nuevos campos, cambios en UI y reglas que faltan por integrar para cumplir al 100% con los requisitos oficiales de **MiCarro**.

---

## Índice de Tareas Pendientes

1. [Tarea 1: Soporte para evidencia/adjuntos en Servicios Realizados (Fotos/Facturas)](#tarea-1-soporte-para-evidenciaadjuntos-en-servicios-realizados)
2. [Tarea 2: Exponer campos opcionales de Repuestos en la UI (Proveedor, Garantía, Notas)](#tarea-2-exponer-campos-opcionales-de-repuestos-en-la-ui)
3. [Tarea 3: Flujo de Edición de Actividades/Planes de Mantenimiento](#tarea-3-flujo-de-edición-de-actividadesplanes-de-mantenimiento)
4. [Tarea 4: Edición y Eliminación de Servicios Realizados en el Historial](#tarea-4-edición-y-eliminación-de-servicios-realizados-en-el-historial)
5. [Tarea 5: Indicar Mantenimiento de Origen en Repuestos Instalados](#tarea-5-indicar-mantenimiento-de-origen-en-repuestos-instalados)

---

## Tarea 1: Soporte para evidencia/adjuntos en Servicios Realizados

### Objetivo
Permitir que el usuario adjunte una foto o documento (factura, recibo, soporte de taller) al registrar un servicio realizado.

### Paso a Paso

#### 1.1 Modelo de Dominio
Archivo: `domain/model/MaintenanceService.kt`
* Agregar la propiedad opcional `evidenceUri: String? = null`.

```kotlin
data class MaintenanceService(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val planId: String? = null,
    val title: String,
    val category: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String,
    val evidenceUri: String? = null // NUEVO
)
```

#### 1.2 Entidad Room y Mappers
Archivo: `feature/maintenance/data/MaintenanceEntities.kt`
* Agregar `val evidenceUri: String? = null` a `MaintenanceServiceEntity`.
* Actualizar las funciones `toDomain()` y `toEntity()`.

```kotlin
@Entity(tableName = "maintenance_services")
data class MaintenanceServiceEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val planId: String?,
    val title: String,
    val category: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String,
    val evidenceUri: String? = null
)
```

#### 1.3 Formulario UI (`ServiceFormScreen.kt`)
Archivo: `feature/maintenance/presentation/ServiceFormScreen.kt`
1. Crear un launcher para seleccionar imagen de la galería:
```kotlin
var selectedEvidenceUri by remember { mutableStateOf<Uri?>(null) }
val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    selectedEvidenceUri = uri
}
```
2. Agregar un componente en la UI con vista previa y botón de selección:
```kotlin
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("Soporte / Evidencia (Factura o Foto)", style = MaterialTheme.typography.labelMedium)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(if (selectedEvidenceUri == null) "Adjuntar Foto/Factura" else "Cambiar Foto")
        }
        selectedEvidenceUri?.let { uri ->
            IconButton(onClick = { selectedEvidenceUri = null }) {
                Icon(Icons.Default.Close, contentDescription = "Eliminar evidencia", tint = StatusError)
            }
        }
    }
    selectedEvidenceUri?.let { uri ->
        AsyncImage(
            model = uri,
            contentDescription = "Evidencia de servicio",
            modifier = Modifier.size(100.dp).clip(MaterialTheme.shapes.small)
        )
    }
}
```
3. Al invocar `onSave`, pasar `evidenceUri = selectedEvidenceUri?.toString()`.

---

## Tarea 2: Exponer campos opcionales de Repuestos en la UI

### Objetivo
Permitir que el usuario ingrese Proveedor, Garantía y Observaciones/Notas al añadir un repuesto.

### Paso a Paso

#### 2.1 Formulario de Repuestos (`PartFormScreen.kt`)
Archivo: `feature/parts/presentation/PartFormScreen.kt`

1. Añadir estados locales para los nuevos campos:
```kotlin
var provider by remember { mutableStateOf("") }
var warranty by remember { mutableStateOf("") }
var notes by remember { mutableStateOf("") }
```

2. Añadir los campos de texto opcionales en el formulario:
```kotlin
MiCarroTextField(
    value = provider,
    onValueChange = { provider = it },
    label = "Proveedor / Tienda (Opcional)",
    placeholder = "Autoplanet, Importadora X..."
)
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    MiCarroTextField(
        value = warranty,
        onValueChange = { warranty = it },
        label = "Garantía (Opcional)",
        placeholder = "6 meses / 10.000 km",
        modifier = Modifier.weight(1f)
    )
    MiCarroTextField(
        value = notes,
        onValueChange = { notes = it },
        label = "Observaciones (Opcional)",
        placeholder = "Serie, especificaciones...",
        modifier = Modifier.weight(1f)
    )
}
```

3. Actualizar la creación del objeto `Part`:
```kotlin
Part(
    serviceId = "",
    name = partName.trim(),
    quantity = quantity.toInt(),
    cost = cost.toDouble(),
    brand = brand.trim().ifEmpty { null },
    reference = reference.trim().ifEmpty { null },
    provider = provider.trim().ifEmpty { null },
    warranty = warranty.trim().ifEmpty { null },
    notes = notes.trim().ifEmpty { null }
)
```

---

## Tarea 3: Flujo de Edición de Actividades/Planes de Mantenimiento

### Objetivo
Permitir al usuario editar el título, categoría o intervalos (km/meses) de un plan existente.

### Paso a Paso

#### 3.1 Adaptar `MaintenanceFormScreen.kt` para Edición
Archivo: `feature/maintenance/presentation/MaintenanceFormScreen.kt`
* Aceptar un parámetro opcional `planToEdit: MaintenancePlan? = null`.
* Cargar los valores iniciales de `title`, `category`, `intervalKmText`, `intervalMonthsText` desde `planToEdit` si no es nulo.
* Al guardar:
  - Si `planToEdit == null` $\rightarrow$ invocar `viewModel.savePlan(...)`
  - Si `planToEdit != null` $\rightarrow$ invocar `viewModel.updatePlan(planToEdit.copy(...))`

#### 3.2 Añadir Botón de Edición en la Tarjeta
Archivo: `feature/maintenance/presentation/MaintenancePlanScreen.kt`
1. Agregar el callback `onEdit: () -> Unit` en `MaintenancePlanCard`.
2. Incluir el botón en las acciones de la tarjeta:
```kotlin
IconButton(onClick = onEdit) {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Editar actividad",
        tint = MaterialTheme.colorScheme.primary
    )
}
```

---

## Tarea 4: Edición y Eliminación de Servicios Realizados en el Historial

### Objetivo
Permitir modificar o eliminar registros de servicios anteriores con diálogo de confirmación.

### Paso a Paso

#### 4.1 Extender DAO y Repositorio
Archivos:
- `feature/maintenance/data/MaintenanceServiceDao.kt`:
```kotlin
@Update
suspend fun updateService(service: MaintenanceServiceEntity)

@Query("DELETE FROM maintenance_services WHERE id = :serviceId")
suspend fun deleteService(serviceId: String)
```
- `domain/repository/MaintenanceRepository.kt`:
```kotlin
suspend fun updateService(service: MaintenanceService)
suspend fun deleteService(serviceId: String)
```
- Impleméntalos en `MaintenanceRepositoryImpl.kt`.

#### 4.2 Casos de Uso y ViewModel
Archivos:
- `feature/maintenance/domain/MaintenanceUseCases.kt`:
```kotlin
class UpdateMaintenanceServiceUseCase @Inject constructor(private val repo: MaintenanceRepository) {
    suspend operator fun invoke(service: MaintenanceService) = repo.updateService(service)
}
class DeleteMaintenanceServiceUseCase @Inject constructor(private val repo: MaintenanceRepository) {
    suspend operator fun invoke(serviceId: String) = repo.deleteService(serviceId)
}
```
- En `MaintenanceViewModel.kt`, expón las funciones `updateService` y `deleteService`.

#### 4.3 Diálogo de Confirmación para Eliminar Servicio
En la pantalla de historial o lista de servicios:
```kotlin
if (showDeleteConfirmDialog) {
    AlertDialog(
        onDismissRequest = { showDeleteConfirmDialog = false },
        title = { Text("Eliminar Servicio") },
        text = { Text("¿Estás seguro de eliminar este registro de servicio?") },
        confirmButton = {
            TextButton(onClick = {
                viewModel.deleteService(selectedServiceId)
                showDeleteConfirmDialog = false
            }) {
                Text("Eliminar", color = StatusError)
            }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirmDialog = false }) {
                Text("Cancelar")
            }
        }
    )
}
```

---

## Tarea 5: Indicar Mantenimiento de Origen en Repuestos Instalados

### Objetivo
Mostrar de manera clara a qué servicio realizado o mantenimiento pertenece cada repuesto en la pestaña "Repuestos Instalados".

### Paso a Paso

#### 5.1 Ajustar la Tarjeta de Repuesto Instalado
Archivo: `feature/maintenance/presentation/MaintenancePlanScreen.kt`
* En `InstalledPartCard(part: Part)`, consultar o pasar la información del servicio origen si está disponible, o formatear el `serviceId` / fecha de instalación:

```kotlin
@Composable
fun InstalledPartCard(part: Part) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    MiCarroCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${String.format(Locale.US, "%.2f", part.cost * part.quantity)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!part.brand.isNullOrBlank() || !part.reference.isNullOrBlank()) {
                val brandRef = listOfNotNull(part.brand, part.reference).joinToString(" • ")
                Text(text = brandRef, style = MaterialTheme.typography.bodySmall, color = muted)
            }

            Text(
                text = "Cantidad: ${part.quantity}  (Unitario: $${String.format(Locale.US, "%.2f", part.cost)})",
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )

            // Muestra del proveedor o garantía si existen
            part.provider?.let {
                Text(text = "Proveedor: $it", style = MaterialTheme.typography.bodySmall, color = muted)
            }
            part.warranty?.let {
                Text(text = "Garantía: $it", style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }
    }
}
```

---

## Verificación Final Post-Implementación

Una vez aplicados los cambios, verifica lo siguiente:
1. Compilación limpia ejecutando `./gradlew assembleDebug`.
2. Ejecución exitosa de pruebas unitarias con `./gradlew testDebugUnitTest`.
3. Verificación de UI:
   - Registro de un servicio adjuntando una foto o comprobante.
   - Guardado de un repuesto completando proveedor, garantía y notas.
   - Edición de un plan de mantenimiento existente.
   - Eliminación de un servicio con diálogo de confirmación.
