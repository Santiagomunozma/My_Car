# Maqueta — Rediseño limpio Aqua

Regla en todas las pantallas: 1 acción principal visible, máximo 3 acciones en el primer pantallazo, el resto en ⋮ u hoja inferior. Sin filas de botones de texto en tarjetas.

## Navegación

Antes: 5 pestañas (Inicio, Vehículos, Mantenimiento, Historial, Ajustes).
Después: barra flotante píldora con 4 pestañas + «Más». «Más» abre una hoja inferior.

```
┌──────────────────────────────┐
│  (icono) Ajustes          ›  │
│  (icono) Alertas documentos› │
│  (icono) Alertas mant.     › │
│  (icono) Respaldo          › │
│  (icono) Exportar historial› │
└──────────────────────────────┘
 [ Inicio  Vehículos  Mant.  Historial  Más ]
```

## Inicio

Antes: header con línea degradada, selector, 3 tarjetas que abren diálogo, dos listas.
Después:

```
Mi Carro                                   (campana)
┌──────────────────────────────┐
│  FOTO DEL VEHÍCULO           │
│  Toyota Corolla · ABC123     │
│  48.200 km · $120 este mes   │
└──────────────────────────────┘
 Próximo mantenimiento
┌──────────────────────────────┐
│ Cambio de aceite             │
│ 12 nov · en 1.800 km    (•)  │
└──────────────────────────────┘
 [Servicio]   [Kilometraje]   [Documentos]   <- tiles con icono
 Recientes
  Cambio de aceite ......... $85
```

## Vehículos

Antes: tarjeta con Editar / Kilometraje / Documentos / Principal / Archivar en fila.
Después: tocar la tarjeta abre el vehículo; ⋮ reúne Kilometraje, Documentos, Principal, Archivar.

```
┌──────────────────────────────┐
│ [foto] ABC123             ⋮  │
│        Toyota Corolla        │
│        48.200 km             │
└──────────────────────────────┘
                                          [+]
```

## Mantenimiento

Antes: tabs de placas + tabs Planes/Repuestos + 4 iconos sin texto + botón por tarjeta.
Después: selector compacto de vehículo, un segmentado, ⋮ por plan.

```
 Mantenimiento                            ⋮
 [ Toyota ABC123  v ]
 ( Planes | Repuestos )
┌──────────────────────────────┐
│ Cambio de aceite          ⋮  │
│ Cada 5.000 km · 12 nov       │
│ [ Registrar servicio ]       │
└──────────────────────────────┘
```

## Historial

Antes: resumen + 7 bloques de filtros empujan la lista fuera de la pantalla.
Después: la lista se ve al entrar; filtros en hoja.

```
 Historial                                ⋮
 [ Resumen de gastos: $420 este mes ]
 [ Buscar...              ] [Filtros (2)]
  Cambio de aceite ......... $85    ⋮
  Filtros .................. $30    ⋮
```

## Ajustes (desde «Más»)

```
 Ajustes
 Apariencia   ( Claro | Sistema | Oscuro )
 ┌──────────────────────────────┐
 │ Alertas                   ›  │
 │ Respaldo                  ›  │
 └──────────────────────────────┘
 ┌──────────────────────────────┐
 │ Borrar datos        (rojo)   │
 └──────────────────────────────┘
```
