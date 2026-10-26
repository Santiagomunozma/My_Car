# Sistema de diseño — Mi Carro

Identidad tomada del logo real (`app/src/main/res/drawable/ic_logo.xml`): monograma en degradado de `#38BDF8` a `#818CF8`, hexágono con trazo cian, fondo navy y dos barras inferiores (cian más larga, índigo más corta).

El violeta de referencia `#8B7CF6` no está en el archivo. El acento secundario es el índigo del logo, `#818CF8`.

La implementación es Jetpack Compose Material 3. Los tokens viven en `app/src/main/java/com/micarro/ui/theme/`. No hay variables CSS.

## Color

### Marca

| Token | Hex | Uso |
| --- | --- | --- |
| Cian | `#38BDF8` | Degradado, foco, iconos sobre fondo oscuro |
| Índigo | `#818CF8` | Final del degradado y acento |
| Degradado | `#38BDF8` → `#818CF8` a 135° | Botón principal, header de inicio, énfasis corto |
| Primario de acción en claro | `#0369A1` | Enlaces, bordes activos y texto sobre superficie clara |

`#0284C7` con texto blanco queda en ~4.1:1, por debajo de 4.5:1. Por eso el primario interactivo en claro es `#0369A1` (~5.9:1 con blanco). El degradado claro lleva texto `#0B1120`, que supera 4.5:1 en los dos extremos.

### Modo oscuro

| Rol | Hex |
| --- | --- |
| Fondo | `#0B1120` |
| Superficie | `#111A2E` |
| Superficie elevada | `#17233D` |
| Borde decorativo | `#243352` |
| Borde de control | `#64748B` |
| Texto | `#F1F5F9` |
| Texto secundario | `#94A3B8` |
| Primario | `#38BDF8` sobre texto `#0B1120` |

### Modo claro

| Rol | Hex |
| --- | --- |
| Fondo | `#F5F8FC` |
| Superficie | `#FFFFFF` |
| Borde decorativo | `#E2E8F0` |
| Borde de control | `#64748B` |
| Texto | `#0F172A` |
| Texto secundario | `#475569` |

En claro las tarjetas tienen sombra de 1 dp y borde suave. En oscuro la sombra baja a 0 y el borde separa la superficie.

### Estados

Siempre con icono y texto. El color no es el único canal.

| Tono | Claro (texto / contenedor) | Oscuro (texto / contenedor) |
| --- | --- | --- |
| Éxito | `#065F46` / `#D1FAE5` | `#A7F3D0` / `#064E3B` |
| Advertencia | `#92400E` / `#FEF3C7` | `#FDE68A` / `#78350F` |
| Error | `#9F1239` / `#FFE4E6` | `#FECDD3` / `#881337` |
| Información | `#0C4A6E` / `#E0F2FE` | `#E0F2FE` / `#0C4A6E` |

Uso en la app:

- Mantenimiento: al día (éxito), próxima (advertencia), vencida (error), sin programar (información), pausado (neutro de superficie).
- Documento: vigente, por vencer, vencido. Mismos tres tonos.
- Repuesto: no hay inventario. La garantía se muestra como texto. No se inventan estados de stock.

### Tema

`ThemeMode`: sistema, claro, oscuro. Se guarda en `SharedPreferences` (`ui_prefs`) y se lee antes de `setContent` para no pintar un frame con el modo contrario. El cambio de tema anima el fondo en 200 ms, o en 0 ms si la escala de animación del sistema es 0.

## Tipografía

Plus Jakarta Sans (latin, OFL) en `res/font`. JetBrains Mono Medium para placa, VIN y kilometraje.

| Token | Tamaño | Alto | Peso | Rol Material |
| --- | --- | --- | --- | --- |
| xs | 12 | 16 | Medium | labelSmall, labelMedium |
| sm | 14 | 20 | Regular / SemiBold | bodyMedium, labelLarge |
| base | 16 | 24 | Regular | bodyLarge |
| lg | 18 | 26 | SemiBold | titleMedium |
| xl | 22 | 28 | SemiBold | titleLarge |
| 2xl | 28 | 34 | SemiBold | headlineMedium |
| 3xl | 32 | 40 | SemiBold | headlineLarge |

## Forma y espacio

| Uso | Radio |
| --- | --- |
| Controles | 8 dp |
| Tarjetas | 12 dp |
| Diálogos | 16 dp |

Espacio en pasos de 4 dp: 4, 8, 12, 16, 24, 32.

Foco: anillo de 2 dp en el cian de marca. Hover y pressed usan el ripple de Material, 200 ms.

## Componentes

| Componente | Variantes |
| --- | --- |
| `PrimaryButton` | Degradado, ancho completo, loading, deshabilitado |
| `SecondaryButton` | Contorno |
| `GhostButton` | Solo texto |
| `DestructiveButton` | Fondo de error |
| `MiCarroTextField` / `DateField` / desplegable | Borde de control, error con texto |
| `MiCarroCard` | Superficie + borde; sombra solo en claro |
| `StatusChip` | Tono + icono + texto |
| `MiCarroTopBar` | Título, subtítulo opcional, atrás, acciones |
| `EmptyState` | Icono, título, mensaje, acción opcional |
| `LoadingState` | Indicador centrado |
| `MiCarroSkeleton` | Bloque de carga |

Iconos: Material Icons, tamaños 20 dp en controles y 24 dp en navegación.

## Voz

Español, tuteo, una mayúscula inicial en títulos y botones. Detalle en `GLOSARIO_UI.md`.
