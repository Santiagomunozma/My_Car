# Glosario de interfaz — Mi Carro

Una sola palabra por concepto. El nombre de la app es **Mi Carro**. En el resto de la interfaz se dice **vehículo**.

## Términos

| Usar | No usar | Significado |
| --- | --- | --- |
| Mi Carro | My Car, MiCarro, Carro | Nombre de la aplicación |
| Vehículo | Carro, auto, coche | El registro (automóvil, camioneta o motocicleta) |
| Mantenimiento | Reparaciones, arreglos | Módulo de planes y servicios |
| Actividad | Plan suelto, tarea, arreglo | Un plan preventivo concreto |
| Servicio | Reparación, arreglo | Trabajo ya realizado, preventivo o correctivo |
| Servicio preventivo | Mantenimiento programado (como tipo) | Tipo de servicio ligado a una actividad |
| Servicio correctivo | Reparación, arreglo | Tipo de servicio fuera del plan |
| Repuesto | Pieza, refacción, parte | Repuesto instalado en un servicio. No es inventario |
| Documento | Papel, archivo | SOAT, técnico-mecánica, impuesto, etc. |
| Kilometraje | Km suelto como título | Lectura del odómetro. La unidad se escribe `12 345 km` con espacio |
| Copia de seguridad | Backup | Exportar o restaurar los datos |
| Ajustes | Settings, configuración (como título de la pestaña) | Pestaña de preferencias |
| Inicio | Dashboard, home | Primera pestaña |

"Plan" puede aparecer en "Guardar plan" porque nombra la acción de persistir la actividad. El título de pantalla del módulo es **Mantenimiento**. El botón flotante dice **Nueva actividad**.

## Trato y mayúsculas

- Tuteo en toda la app: "tu vehículo", "agrega un documento", "¿Eliminas este servicio?".
- Títulos, botones, menús y etiquetas: solo la primera letra en mayúscula. "Agregar vehículo", no "Agregar Vehículo".
- Mayúsculas sostenidas solo en siglas escritas así por el idioma: SOAT, VIN, PDF, CSV. Las placas se muestran en mayúsculas porque así se leen, no como estilo de botón.
- No escribir textos enteros en mayúsculas dentro de `strings.xml`.

## Verbos de acción

| Situación | Verbo |
| --- | --- |
| Crear un vehículo, un documento o una actividad | Agregar |
| Anotar una lectura o un servicio que ya ocurrió | Registrar |
| Persistir un formulario | Guardar |
| Quitar un registro | Eliminar |
| Ocultar un vehículo sin borrar su historial | Archivar |

No usar "Añadir".

## Formatos

- Fechas cortas: `dd/mm/aaaa` (el selector de fecha ya usa ese patrón).
- Moneda: símbolo `$` y miles con separador de la configuración regional del dispositivo, dos decimales.
- Kilometraje: número con separador de miles, espacio y `km`. Ejemplo: `12.345 km` en es-CO.
- Placas: mayúsculas en el campo y en las listas.

## Mensajes

El vacío dice qué falta y qué hacer.

- Documentos: "Aún no tienes documentos. Agrega el primero para recibir alertas de vencimiento."
- Vehículos: "Aún no tienes vehículos. Agrega el primero para llevar el kilometraje y los documentos."
- Errores: dicen qué falló y qué puede hacer la persona. No usar "Error" solo, ni "OK".

## Estados que sí existen

| Ámbito | Estados |
| --- | --- |
| Actividad de mantenimiento | Al día, Próxima, Vencida, Sin programar, Pausado |
| Documento | Vigente, Por vencer, Vencido |
| Repuesto | Sin semáforo de stock. Se muestra nombre, cantidad y garantía en texto |

No hay pantalla de inicio de sesión.
