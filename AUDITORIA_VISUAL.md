# Auditoría visual — Mi Carro

Fecha: 26 de octubre de 2026.  
Alcance: app Android (Jetpack Compose + Material 3). No hay capa web, Tailwind ni CSS global.  
La interfaz real está en `app/src/main/java/com/micarro/ui/` y en las pantallas de `feature/**/presentation/`. Los textos centralizados están en `app/src/main/res/values/strings.xml`.

Esta auditoría describe el estado **anterior** al rediseño.

## Resumen

La marca del logo (cian `#38BDF8`, índigo `#818CF8`, navy `#0B0F19`) no es la marca de la interfaz (rojo `#C41E3A`). El tema oscuro solo sigue al sistema, la tipografía es la del sistema y está a medias, y varios textos visibles están hardcodeados o en Title Case. Los chips de estado usan texto blanco sobre verdes, ámbar y cian que no llegan a WCAG AA.

## Tabla de hallazgos

| Archivo | Problema | Severidad | Solución propuesta |
| --- | --- | --- | --- |
| `ui/theme/Color.kt` | Primario rojo `#C41E3A` / `#E11D48` y contenedores rosados `#FDE8EC`, `#4A1520`. No coincide con el logo. | Alta | Sustituir por escala cian/índigo del logo, con primario claro más profundo para contraste. |
| `res/drawable/ic_logo.xml` | Logo real: navy `#0B0F19`→`#1B2436`, trazo `#38BDF8`/`#1E3A8A`, monograma `#38BDF8`→`#818CF8`. El violeta del encargo (`#8B7CF6`) no está en el archivo. | Alta | Usar `#818CF8` como acento secundario, no inventar otro violeta. |
| `res/values/colors.xml` | `primary` sigue en `#C41E3A`. | Media | Alinear el color XML de splash y ventana con el fondo del tema. |
| `ui/components/StatusChip.kt` | Texto e icono fijos en `Color.White` sobre el color de estado. | Alta | Contenedor suave + texto e icono del tono, ambos con contraste AA. |
| `ui/theme/Color.kt` (`StatusSuccess` `#16A34A`) | Blanco sobre ese verde ≈ 3.2:1. Falla en texto de 12 sp. | Alta | Verde esmeralda más oscuro para texto; contenedor claro/oscuro aparte. |
| `ui/theme/Color.kt` (`StatusInfo` `#0891B2`) | Blanco sobre cian ≈ 3.6:1. Falla AA. | Alta | Texto info sobre contenedor, no blanco sobre el tono puro. |
| `ui/theme/Color.kt` (`StatusWarning` `#D97706`) | Blanco sobre ámbar ≈ 4.2:1. Falla para texto normal. | Alta | Ámbar oscuro sobre contenedor crema (claro) o ámbar claro sobre marrón (oscuro). |
| `ui/theme/Color.kt` (`StatusError` `#DC2626`) | Blanco sobre rojo ≈ 4.8:1. Pasa justo; no tiene variante de contenedor. | Media | Rojo coral con par contenedor / contenido en cada modo. |
| `ui/theme/Color.kt` (`OnSurfaceVariantLight` `#6B7280` sobre `#F3F4F6`) | Contraste ≈ 4.4:1. Falla por poco el texto normal. | Media | Texto secundario `#475569` sobre `#F5F8FC` (supera 4.5:1). |
| `ui/theme/Color.kt` (bordes `#D1D5DB` y `#3D4658`) | Contraste del borde ≈ 1.5:1. No sirve como único indicador de foco. | Media | Borde decorativo suave y borde de foco más oscuro, con anillo visible. |
| `ui/theme/Theme.kt` | El `ColorScheme` no define tertiary, errorContainer ni outlineVariant. Los estados se importan sueltos. | Alta | Esquema completo y `CompositionLocal` de estados. |
| `feature/vehicle/presentation/VehicleColors.kt` | Siete colores de carrocería hardcodeados (`#FFFFFF`, `#1C1C1C`, `#E53935`, etc.). | Baja | Dejarlos: son el color del vehículo, no de la marca. |
| `feature/dashboard/presentation/DashboardScreen.kt` | Radios sueltos 16, 12, 6 y 3 dp y `Card` de Material en lugar de `MiCarroCard`. | Media | Usar radios del sistema (8 / 12 / 16) y la tarjeta común. |
| `feature/maintenance/presentation/components/ExpensesSummaryCard.kt` | Igual: `Card` y radios locales, título en Title Case. | Media | Misma tarjeta y título en mayúscula de frase. |
| `ui/theme/Type.kt` | Solo 7 estilos, todos `FontFamily.Default`. No hay `titleSmall`, `labelSmall` ni `headlineMedium`. | Media | Escala completa con Plus Jakarta Sans y una mono para placa, VIN y km. |
| `DashboardScreen.kt`, `ExpensesSummaryCard.kt` | `FontWeight.Bold` encima del estilo del tema. | Baja | Pesos solo dentro de la escala tipográfica. |
| `ui/theme/Shape.kt` | Radios 16–36 dp en todos los tamaños. Muy redondo y distinto del logo. | Media | 8 dp controles, 12 dp tarjetas, 16 dp diálogos. |
| `ui/components/Buttons.kt` | Primario, secundario y destructivo siempre a ancho completo. Sin ghost, icono, loading ni tamaño compacto. | Alta | Variantes primary, secondary, ghost y danger, con loading y altura mínima de 48 dp. |
| `MaintenancePlanScreen.kt`, `MaintenanceFormScreen.kt`, `PartFormScreen.kt`, `SettingsScreen.kt` | `Button` de Material suelto para la misma acción que `PrimaryButton`. | Alta | Un solo botón por función. |
| `MaintenancePlanScreen.kt` | El FAB fuerza `primary` / `onPrimary`; vehículos y documentos no. | Baja | Un solo estilo de FAB. |
| `DashboardScreen.kt` | Header propio ("MiCarro") frente al `TopAppBar` del resto. | Media | Mismo patrón de título, subtítulo y acciones. |
| `DashboardScreen.kt` | Textos hardcodeados en Title Case: "Próximos Mantenimientos", "Servicios Recientes", "Vehículo Activo", "Alertas y Notificaciones", "N/A", "Gastos Mes". | Alta | Frases con una sola mayúscula inicial, desde `strings.xml`. |
| `strings.xml` | `app_name` = "My Car", `dashboard_title` = "MiCarro", `status_ok` = "OK". | Alta | Marca "Mi Carro" y el resto en español. |
| `strings.xml` | "Agregar", "Añadir", "Registrar", "Nueva actividad" y "Nuevo plan" para acciones parecidas. | Media | Agregar para altas; Registrar para lecturas y servicios ya ocurridos; plan = actividad de mantenimiento. |
| `strings.xml` | Bloques duplicados `maintenance_*` y `plan_*`. | Media | Una sola clave por mensaje visible. |
| `strings.xml` | `maintenance_delete_message` usa "¿Deseas…?" y el resto tutea. | Baja | Tuteo en toda la app. |
| `VehicleFormScreen.kt` | "Tipo de Vehículo", "CARRO", "MOTO" y ayudas de placa/VIN hardcodeadas. Ya existen `field_type` y `vehicle_type_*`. | Alta | Usar los recursos y los tipos ya definidos (automóvil, camioneta, motocicleta). |
| `MaintenanceFormScreen.kt` | Plantillas en Title Case ("Cambio de Aceite") y "Volver" hardcodeado. | Media | Plantillas en mayúscula de frase y `action_back`. |
| `ExpensesSummaryCard.kt`, `HistoryScreen.kt`, `PartFormScreen.kt` | "Resumen de Gastos", "Mes", "6 Meses", "Preventivo", "Ref:" fuera de recursos. | Media | Moverlos a `strings.xml`. |
| `HistoryViewModel.kt`, `BackupViewModel.kt`, `SettingsViewModel.kt` | Mensajes de éxito y error armados en Kotlin, distintos a las cadenas existentes. | Media | Esas cadenas, con tuteo y una sola redacción. |
| `plan_parts_empty_message` | Dice "repuestos y piezas". | Baja | Solo "repuestos". |
| `ui/components/EmptyState.kt` | Sin acción y sin variante de error. El icono no tiene descripción porque el título ya se lee. | Media | Slot de acción opcional. |
| `DashboardScreen.kt`, `HistoryScreen.kt` | El estado de carga existe en el ViewModel y no se muestra. El vacío es un texto suelto. | Alta | Indicador de carga y vacío con título y mensaje. |
| `DocumentsAlertsScreen.kt`, `MileageScreen.kt`, `MaintenancePlanScreen.kt` | Vacío sin indicador de carga. | Media | Spinner mientras `isLoading`. |
| `SettingsScreen.kt` | `errorMessage` no se pinta. Borrar datos no usa el botón destructivo común. | Alta | Banner de error y `DestructiveButton`. |
| `BackupScreen.kt` | Sin spinner y sin confirmación al restaurar. | Media | Loading en el botón y diálogo de confirmación. |
| `AlertSettingsScreen.kt` | Guardar solo vuelve atrás, sin confirmación. | Baja | Aviso breve de que los cambios quedaron guardados. |
| `ui/theme/Theme.kt` | `darkTheme` solo con `isSystemInDarkTheme()`. Sin preferencia de usuario. | Alta | Claro, oscuro y sistema, leído antes de `setContent`. |
| `res/values/themes.xml` | `windowLightStatusBar=true` fijo en el tema de día. Si el usuario elige oscuro con el sistema en claro, hay un instante de barras incorrectas. | Media | Resolver el modo antes de `enableEdgeToEdge`. |
| `ui/theme/Color.kt` | Éxito, aviso, error e info son el mismo hex en ambos modos. | Media | Pares contenedor/contenido por modo. |
| Todas las pantallas | No hay `WindowSizeClass` ni `sw600dp`. Una columna con padding fijo. | Media | Barra inferior en compacto y barra lateral desde 600 dp, mismas rutas. |
| `EmptyState.kt`, `StatusChip.kt`, `DateField.kt`, `DashboardScreen.kt`, `HistoryScreen.kt` | `contentDescription = null` en iconos de acción o diálogos, y descripciones hardcodeadas. | Media | Descripciones en recursos para iconos con acción; decorativos siguen en null si el texto adyacente se lee. |
| `StatusChip.kt` | Padding vertical 6 dp. Si el chip se pulsa, el área queda por debajo de 48 dp. | Baja | Alto mínimo de 48 dp solo cuando es pulsable. Los chips informativos pueden ser más bajos si el texto y el icono acompañan el color. |
| `DashboardScreen.kt` | El resumen no muestra documentos por vencer ni repuestos por agotarse. El caso de uso no trae esos datos, y los repuestos no son un inventario. | Baja | No inventar datos. Reordenar lo que el resumen ya trae: alertas, próximos planes, servicios y gastos. |

## Conteos de color (antes del rediseño)

- Tokens de tema en `Color.kt`: 22.
- XML en `colors.xml`: 3 (`primary`, `black`, `white`).
- Logo y fondo del launcher: 5 (`#0B0F19`, `#1B2436`, `#1E3A8A`, `#38BDF8`, `#818CF8`).
- Muestras de carrocería: 7, ajenas a la marca.
- Blanco fijo fuera del esquema: `StatusChip`.

## Tipografía observada

| Rol | Peso | Tamaño / alto | Notas |
| --- | --- | --- | --- |
| titleLarge | SemiBold | 22 / 28 | Definido |
| titleMedium | SemiBold | 16 / 24 | Definido |
| bodyLarge | Normal | 16 / 24 | Definido |
| bodyMedium | Normal | 14 / 20 | Definido |
| bodySmall | Normal | 12 / 16 | Definido |
| labelLarge | SemiBold | 14 / 20 | Definido |
| labelMedium | Medium | 12 / 16 | Definido |
| titleSmall, labelSmall, headlineMedium | Default Material 3 | Sin override | Inconsistentes entre pantallas |

Familia: Roboto del sistema. No hay mono para placas ni kilometraje.

## Estados de interfaz por pantalla

| Pantalla | Carga | Vacío | Error | Confirmación |
| --- | --- | --- | --- | --- |
| Inicio | Flag sin UI | Textos parciales | No | No aplica |
| Vehículos | Spinner | EmptyState | No | Archivar |
| Formulario de vehículo | Spinner | No aplica | Por campo | No |
| Kilometraje | No | EmptyState | Validación | Lectura menor |
| Documentos | Spinner | EmptyState | Lectura y guardado | Eliminar |
| Alertas documentales | Oculta el vacío | EmptyState | No | No |
| Mantenimiento | No | EmptyState | Diálogo de bloqueo | Eliminar y posponer |
| Historial | Flag sin UI | Texto suelto | Snackbar de exportar | Eliminar servicio |
| Ajustes | Spinner en el botón | No aplica | Mensaje no visible | Eliminar todo |
| Copia de seguridad | Botones deshabilitados | No aplica | Snackbar | Restaurar sin confirmar |
| Alertas de mantenimiento | No | No aplica | No | Guardar sin aviso |

## Responsive y accesibilidad

La navegación inferior tiene cinco destinos y se oculta en formularios. No hay rail ni panel para tablet. Los botones base miden 52 dp. El foco visible es el de Material 3, sin anillo de marca. Parte de las descripciones de iconos no están en recursos.
