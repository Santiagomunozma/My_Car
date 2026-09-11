# MiCarro — Guía de Integración UI/UX para el Equipo

Esta guía establece las reglas visuales y de diseño para garantizar que MiCarro se sienta como un solo producto homogéneo. Ningún módulo debe definir colores HEX ni radios de curvatura independientes.

---

## 1. Paleta Oficial de Colores

Consume los colores semánticos centralizados en `com.example.my_car.ui.theme.*`[cite: 3]:

* **PrimaryBlue (`#2563EB`):** Acciones principales y elementos activos de navegación[cite: 3].
* **PrimaryDarkBlue (`#1D4ED8`):** Estados presionados o interacción[cite: 3].
* **BackgroundLight (`#F8FAFC`):** Fondo general de las pantallas[cite: 3].
* **SurfaceWhite (`#FFFFFF`):** Fondo de tarjetas (Cards) y contenedores de formulario[cite: 3].
* **TextPrimary (`#111827`):** Títulos de pantalla y contenido principal[cite: 3].
* **TextSecondary (`#6B7280`):** Metadatos, etiquetas y textos de ayuda[cite: 3].
* **BorderGray (`#E5E7EB`):** Divisores y bordes de inputs[cite: 3].

### Estados Semánticos (Mantenimientos y Alertas)
* **StatusSuccess (`#16A34A`):** Estado "Al día"[cite: 3].
* **StatusWarning (`#D97706`):** Estado "Próxima"[cite: 3].
* **StatusError (`#DC2626`):** Estado "Vencida" o acciones destructivas[cite: 3].
* **StatusInfo (`#0891B2`):** Mensajes informativos[cite: 3].

---

## 2. Radios y Espaciados (`AppShapes`)

Aplica formas estándar mediante `MaterialTheme.shapes`[cite: 3]:
* **Small (12dp):** Campos de texto (`TextField`) y botones generales[cite: 3].
* **Medium (16dp):** Tarjetas (`Card`) de vehículos, mantenimientos o resumen[cite: 3].
* **Large (20dp):** Diálogos de confirmación y `StatusChip`[cite: 3].
* **Espaciados:** Basados en cuadrícula de 4dp (4, 8, 12, 16, 24, 32dp)[cite: 3].

---

## 3. Uso del Componente Reutilizable `StatusChip`

Para cumplir accesibilidad (WCAG AA), **un estado nunca se comunica solo con color**[cite: 3]. Importa y usa el componente centralizado[cite: 3]:

```kotlin
import com.example.my_car.ui.components.StatusChip
import com.example.my_car.ui.theme.StatusSuccess
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle

StatusChip(
    text = stringResource(R.string.status_up_to_date),
    containerColor = StatusSuccess,
    icon = Icons.Default.CheckCircle
)
