# Contexto de My Car para armar la ponencia

Documento de briefing. Está sacado del código y la documentación del repositorio (noviembre 2026). Lo que el repo no dice está en **Huecos** al final: no lo inventes.

---

## La app

**Nombre de producto:** My Car.  
Es el mismo texto en el icono del teléfono y en el título de Inicio. Paquete: `com.micarro`. Versión **1.0** (`versionCode` 1). No escribas «Mi Carro» ni «Mycar».

**Qué hace:** aplicación Android para el dueño de uno o varios vehículos. Lleva el kilometraje, el plan de mantenimiento (por fecha y por kilómetros), los documentos que vencen en Colombia, el historial de gastos y un asistente que responde con ese historial. Todo queda en el teléfono. No hay cuenta ni servidor propio. La versión gratuita muestra un anuncio. My Car Premium, de pago único, lo quita y abre el asistente, las fotos de los documentos, Excel, PDF y la copia de seguridad.

**Problema que resuelve:** el dueño tiene el SOAT en un PDF, la técnico-mecánica en otro lado, el aceite “cuando se acuerde” y los recibos del taller en WhatsApp. Es fácil circular con un documento vencido o pasar el kilometraje del cambio de aceite sin notarlo. Al vender el vehículo o llegar al taller no hay un historial único de qué se hizo, cuánto costó y qué repuesto se instaló.

La app junta eso en un solo lugar, avisa antes del vencimiento y deja exportar el historial.

---

## Usuarios

Va dirigida a **conductores en Colombia** que tienen automóvil, camioneta o motocicleta, sobre todo quien lleva más de un vehículo (carro y moto) o quiere dejar constancia de lo que se le hizo al carro.

No es para talleres ni para flotas empresariales. No hay roles, ni inicio de sesión, ni multiusuario.

Supuestos de diseño:

- Habla en español, de tú.
- Placas colombianas: carro `AAA123` (3 letras y 3 números); moto `AAA12A` (3 letras, 2 números y 1 letra).
- Documentos locales: SOAT, revisión técnico-mecánica, seguro, impuesto vehicular, semaforización y “otro”.
- Catálogo de marcas y líneas que se venden en Colombia (`vehicles_colombia.json`), más tipo de combustible (gasolina, diésel, gas, eléctrico, híbrido).
- Montos en pesos, con `$` y el separador del teléfono.
- Los datos viven en el dispositivo. La copia de seguridad es un archivo que la persona guarda donde quiera. Exportarla y restaurarla es de Premium o de la prueba.

---

## Funciones principales

Las cuatro que más cuentan en una demo:

### 1. Inicio del vehículo activo

Héroe con el vehículo principal (foto, placa, kilometraje), próximo mantenimiento, gastos del mes, accesos rápidos (registrar servicio, kilometraje, documentos) y servicios recientes. Se puede cambiar de vehículo activo. En pantallas anchas (≥ 600 dp) la barra inferior pasa a un rail lateral.

### 2. Plan de mantenimiento preventivo y servicios

Actividades con intervalo en kilómetros, en meses, o ambos. Plantillas: cambio de aceite, filtros, frenos, batería. Estados: al día, próxima, vencida, sin programar, pausada. Una actividad con historial no se borra; se pausa.

Al registrar un servicio (preventivo o correctivo) se anotan fecha, kilometraje, taller, mano de obra, otros costos, total y fotos de soporte. Los repuestos instalados llevan marca, referencia, proveedor, cantidad, precio y garantía (meses y/o km). Hay aviso de garantía por vencer.

Si el kilometraje es menor que el anterior, o la fecha es anterior al último servicio, la app pide confirmación. No borra el historial en silencio.

### 3. Documentos colombianos y alertas

Alta de SOAT, técnico-mecánica, seguro, impuesto, semaforización u otro, con vencimiento, emisor y número. El estado se calcula: vigente, por vencer, vencido. Escribir esos datos a mano es gratis.

**Tomar foto o adjuntar PDF** (tope 8 MB) es de Premium o de la prueba. Gemini lee el archivo (tipo, vencimiento, emisor, número, placa). Si la placa del papel no coincide con la del vehículo, avisa y pide confirmación. Si no hay clave de Gemini, el archivo se guarda y los campos se llenan a mano. Un archivo que ya se guardó se puede seguir abriendo aunque la prueba haya vencido. No se borra.

Alertas locales: una a 30 días, otra a 14 y otra el día del vencimiento. Switch global y por documento. Anticipación configurable. WorkManager programa las notificaciones en el teléfono (no hay push de un servidor).

### 4. Historial, gastos y salida de datos

Búsqueda y filtros (vehículo, taller, tipo, rango de fechas y de costo). Resumen de gastos: mes, 6 meses, año o todo. El **CSV** es gratis. **Excel (xlsx) y PDF** son de Premium o de la prueba.

Copia de seguridad: exporta y restaura vehículos, kilometraje, documentos, planes, servicios, repuestos y fotos. También es de Premium o de la prueba. Restaurar actualiza por identificador y no duplica placas. Borrar todos los datos del teléfono es irreversible y no quita Premium: el plan vive en otras preferencias, no en la base de datos.

**Quinta pieza, útil en la ponencia:** el asistente (hoja **Más**). Es de Premium o de la prueba. Si no hay acceso, esa fila abre el panel de Premium. Chat con Gemini 2.5 Flash. Recibe el historial de servicios y repuestos del vehículo activo y orienta causas probables. Si un arreglo reciente explica el síntoma, lo dice. No inventa que se cambió una pieza. No da pasos para desarmar frenos, dirección, suspensión, airbags, combustible ni el sistema eléctrico. Ante frenos, dirección, humo, pérdida de líquido u olor a quemado, pide no circular e ir al taller. Aclara que es orientación, no diagnóstico. Un botón abre Google Maps con la búsqueda “taller mecánico”; la app no tiene directorio propio de talleres.

---

## El resto de la app (para no omitirlo)

| Módulo | Qué hace |
| --- | --- |
| Vehículos | Alta y edición, foto, un solo principal, archivar sin perder historial, reactivar. Campos: placa, tipo, marca, línea, modelo, año, color, VIN (17 caracteres), combustible, cilindraje. El kilometraje del formulario no se edita a mano: sale de las lecturas. |
| Kilometraje | Lecturas de odómetro con fecha y nota. Una lectura menor exige confirmación. |
| Alertas de mantenimiento | Switch, días de anticipación y kilómetros de anticipación. Notificación local al abrir la alerta. |
| Ajustes | Tema sistema / claro / oscuro, sin flash al abrir. Fila My Car Premium. Cargar datos de ensayo. Borrar todo. |
| Navegación | 4 pestañas: Inicio, Vehículos, Mantenimiento, Historial. **Más** no es una pestaña: abre hoja con My Car Premium, Asistente, Ajustes, Alertas documentales, Alertas de mantenimiento y Copia de seguridad. |
| My Car Premium | Pantalla de comparación Gratis / Premium. Pago único simulado de **$20.000**. Prueba de **30 días**, una sola vez. Se vuelve a abrir desde los tres puntos o desde Ajustes, también con la prueba ya activa: ahí se ven los días que faltan. |

Identidad visual **Aqua**, tomada del logo: degradado cian `#38BDF8` → índigo `#818CF8`. Fondo oscuro `#0B1120`. Primario de acción en claro `#0369A1` (contraste con texto blanco). Tipografía Plus Jakarta Sans; placa, VIN y kilómetros en JetBrains Mono. Tarjetas 20–24 dp, botones 16 dp, hojas 28 dp. Una acción principal por pantalla. Voz: tuteo, una sola mayúscula inicial (“Agregar vehículo”, no “Agregar Vehículo”).

---

## Tecnología

Aplicación **nativa Android**. No es Flutter, Kotlin multiplatform ni React Native. No usa Firebase.

| Área | Tecnología |
| --- | --- |
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | Clean Architecture + MVVM. UI → dominio (casos de uso, sin Android) → datos |
| Inyección | Hilt 2.60.1 |
| Base de datos | Room 2.7.2, archivo `my_car_db`, versión de esquema 5. Entidades: vehículo, kilometraje, documento, plan, servicio, repuesto |
| Preferencias | SharedPreferences (tema, alertas y el plan Premium: `premium_prefs`) |
| Segundo plano | WorkManager: documentos, mantenimiento, garantía |
| Imágenes | Coil |
| Anuncios | Google Mobile Ads, ids de **prueba**. Banner de 50 dp en Inicio, Vehículos, Mantenimiento e Historial, solo sin Premium ni prueba. Si no carga, una franja que dice «Anuncio». Al activar la prueba o el pago, la franja desaparece |
| Red | Gemini cuando se usa el asistente o la lectura de un documento. El banner de la versión gratuita también pide internet. El resto funciona sin conexión |
| Modelo | `gemini-2.5-flash`. La clave va en `local.properties` como `GEMINI_API_KEY` y se embebe en el APK al compilar. No va en el repositorio |
| Excel | Apache POI 5.2.5 |
| Navegación | Navigation Compose |
| Build | Android Gradle Plugin 9.3.3, Gradle 9.5.0, JDK 17 |
| SDK | `minSdk` **26** (Android 8.0), `targetSdk` / `compileSdk` 37 |
| CI | GitHub Actions: tests unitarios en push y pull request a `develop` y `main` |
| Pruebas | Tests unitarios de reglas de vehículos, kilometraje, documentos, lectura, mantenimiento, historial, Excel, alertas, catálogo, escenario de demo y del plan Premium (gratis, 30 días, vencimiento y pago único) |

Repositorio: https://github.com/Santiagomunozma/My_Car

**Estado de publicación:** prototipo académico funcional, no está en Play Store. El APK de la ponencia se firma con un keystore de demostración (`ponencia/micarro-ponencia.keystore`). Sirve para instalar a mano. No sirve para la tienda. Si el teléfono ya tiene la build de Android Studio, el script de ponencia no la reemplaza, para no borrar los datos de la demo.

Licencia: uso académico / proyecto de equipo. No hay licencia open source publicada.

### Qué está a medias

- Las pruebas de UI en emulador no son el flujo habitual del CI (el CI corre `test`, no `connectedAndroidTest`).
- No hay login, nube ni sincronización entre teléfonos. Cambiar de teléfono es restaurar la copia, y esa copia es de Premium.
- El cobro de $20.000 no pasa por un banco ni por Play Store. Es una simulación guardada en el teléfono. Los anuncios son los de prueba de Google, no una campaña real.
- `MileageAlertScheduler` reprograma las actividades al cambiar el odómetro. No hay un aviso suelto solo por el kilometraje.

---

## Evidencia

El repositorio **no tiene** usuarios reales, descargas, entrevistas ni métricas de uso. No afirmes cifras.

Lo que sí existe como evidencia de que funciona:

- La app corre de punta a punta en un dispositivo: vehículos, kilometraje, documentos, planes, servicios, historial, exportación y copia persisten en Room.
- Hay tests unitarios de las reglas de negocio.
- Hay un **escenario de ensayo** cargable desde Ajustes → “Cargar datos de ensayo”. Borra lo que haya en ese teléfono y mete un historial fijo, con fechas relativas al día de la carga, para que el día de la ponencia el aceite siga vencido y la técnico-mecánica siga próxima.
- Hay un guion de instalación en vivo: `presentar.bat` / `presentar.ps1`.

### Escenario de la demo (útil para el storyboard)

Tres vehículos:

| | Principal | Segundo | Archivado |
| --- | --- | --- | --- |
| Placa | KHM452 | XYZ89F | RTR108 |
| Qué es | Chevrolet Onix LTZ 2022, gris, gasolina, 1.400 cc | Yamaha FZ 2.0 2021, negra, 149 cc | Renault Logan Expression 2014, blanco |
| Km | 45.200 | 12.840 | 98.600 |

En el Onix, el día de la carga:

- SOAT de Seguros Bolívar **vencido hace 12 días**.
- Técnico-mecánica (CDA Autopista) **vence en 8 días**.
- Impuesto (Secretaría de Hacienda) vence en 20 días.
- Seguro todo riesgo (Sura) vigente, vence en 140 días.
- Semáforo (Alcaldía) vence en 5 días, pero las alertas de ese documento están apagadas.
- Cambio de aceite **vencido** (pasó la fecha y el kilometraje; último aceite hace 8 meses en Taller del Norte, Mobil 5W30 y filtro Mann, $168.000).
- Revisión de frenos **próxima** (en 6 días). Pastillas Bosch instaladas hace ~6 meses en Frenos Andinos, garantía de 6 meses por vencer. Total $240.000.
- Rotación de llantas al día.
- Batería en pausa.
- Revisión de luces sin programar.
- Hace 18 días: pinchazo en la delantera derecha, Montallantas 68, parche, $35.000 (servicio correctivo).

En la moto: SOAT Axa Colpatria vigente; kit de arrastre próximo; ajuste de cadena hace 45 días en Motos del Centro.

El Logan archivado conserva su último aceite. Sirve para mostrar que archivar no borra el historial.

### Cómo se muestra en el salón

Con el computador en el proyector:

```powershell
.\presentar.bat
```

1. Compila el APK firmado (la primera vez, o de nuevo con `.\presentar.bat -Compilar`).
2. Abre una página en la red local (`ponencia/index.html`) con un código QR. Quien esté en el **mismo Wi-Fi** escanea, descarga `MiCarro.apk` y permite instalar desde el navegador.
3. Si el celular está por USB con depuración, instala la app si no la tiene y abre la pantalla en una ventana (scrcpy) para proyectarla. La primera vez instala scrcpy.

La consola debe quedar abierta para que el QR siga vivo. `Ctrl+C` apaga solo la descarga. También se puede generar el APK en GitHub Actions → **APK ponencia** → artefacto `MiCarro`.

Para ensayar antes: Ajustes → Cargar datos de ensayo.

---

## Competencia

No hay un estudio de mercado en el repo. Esto es contexto para comparar, no un hallazgo medido del equipo. Si el jurado pide fuentes, hay que citarlas aparte.

Apps parecidas, generales (no hechas para Colombia):

| App | Qué cubre bien | Dónde My Car se aparta |
| --- | --- | --- |
| Drivvo | Gastos, combustible, servicios, recordatorios, varias plataformas | My Car es solo Android, sin cuenta. Drivvo no modela SOAT, técnico-mecánica, impuesto ni semáforo, ni placas colombianas |
| Fuelio / aCar / Simply Auto | Bitácora de combustible y servicios, muy usadas | El centro de My Car no es el tanqueo. Es el documento que vence y el plan por km y por fecha, con repuestos y garantía |
| Apps de “recordatorio de SOAT” o consultas al RUNT | Un solo trámite o una consulta oficial | My Car no consulta el RUNT ni paga el SOAT. Guarda la copia del dueño y la cruza con el kilometraje y el taller |
| Carpeta de Drive, notas o Excel | Flexible y ya lo usa mucha gente | No avisa sola, no distingue lectura de odómetro menor, no lee el PDF del SOAT ni arma el estado “vencido / por vencer” |

Diferencia que sí se puede defender con el producto:

1. Hecha para el calendario documental de Colombia, no para un “registration renewal” genérico.
2. El mantenimiento mira **fecha y kilómetros** a la vez, y el repuesto tiene garantía.
3. Los datos no salen del teléfono, salvo la foto o la pregunta que la persona envía a Gemini, y el archivo de copia que ella exporta.
4. Leer el SOAT o la técnico-mecánica desde la foto, y un asistente que mira el historial real del carro (el pinchazo, las pastillas, el aceite), no un chat genérico.
5. Lista para mostrarse en vivo: datos de ensayo + QR + espejo del celular.

Límite honesto: no tiene seguimiento de combustible, ni GPS, ni valor de reventa, ni comunidad, ni tienda, ni sincronización. Esas ausencias no son “próximamente” a menos que el equipo lo diga.

---

## La ponencia

Lo que el repo sí fija:

- Es una **presentación en salón**, con el computador conectado al proyector.
- El público puede instalar la app en el momento si comparte el Wi-Fi del salón.
- El equipo preparó datos de ensayo para que la demo no dependa de tipear un carro desde cero.
- El README la llama proyecto de equipo académico. En el código de navegación el trabajo está partido en “compañero 1” (vehículos, kilometraje, documentos) y “compañero 2” (mantenimiento). Hay al menos un tercer frente (inicio, historial, ajustes, asistente, diseño Aqua). **No hay nombres** en el repo.

Lo que el repo **no** fija (rellenar antes de diseñar diapositivas):

- Minutos exactos.
- Si el público es profesores, jurado, compañeros o inversionistas. El tono del producto (tuteo, demo en vivo, sin pitch de ingresos) encaja con sustentación académica, no con ronda de inversión.
- Plantilla institucional, número de diapositivas, logo de la universidad o rúbrica.

### Guion sugerido si el tiempo es corto (8–12 min)

No es un requisito del curso. Es un orden que el producto ya soporta.

1. Problema en una frase: el SOAT, el aceite y los recibos viven en sitios distintos y se vencen sin aviso.
2. Inicio con el Onix KHM452 ya cargado: km, gasto del mes, próximo mantenimiento. En gratis se ve una franja de anuncio abajo. Cargar el ensayo **no** activa Premium.
3. Documentos: SOAT vencido y técnico-mecánica a 8 días. Los datos se pueden leer en voz alta sin pagar. Adjuntar la foto es el momento de abrir Premium.
4. Mantenimiento: aceite vencido por km y por fecha; pastillas con garantía por vencer; pausar no es borrar.
5. Tres puntos → **My Car Premium**: prueba de 1 mes o pago simulado de $20.000 (Tarjeta o Nequi, sin pedir número). Decir en voz alta que no se hace ningún cargo. Al volver a Inicio, el anuncio ya no está.
6. Historial: el pinchazo como servicio correctivo y el total. Con la prueba activa, Excel o PDF en una frase. El CSV no hace falta Premium.
7. Asistente, ya desbloqueado: “la llanta delantera derecha se desinfló otra vez” y que la respuesta use el parche de hace 18 días. Leer el aviso de que no es un diagnóstico.
8. Cierre: datos en el teléfono, sin cuenta; QR para quien quiera instalarla; qué falta (tienda, nube, cobro real).

Si hay tiempo de más, mostrar el segundo vehículo (la moto) y el Logan archivado.

### Qué no prometer en las diapositivas

- Usuarios, descargas, ingresos o “validado con conductores”, salvo que el equipo tenga esa evidencia fuera del repo.
- Que consulta el RUNT, paga el SOAT o encuentra el taller más cercano con teléfono y horario.
- Que el asistente diagnostica. El texto de la app dice “orientación general”.
- Que el pago de $20.000 es real, que ya hay ingresos o que los anuncios son una campaña contratada. El pie de la hoja de pago dice que es una simulación de la sustentación.
- Play Store o disponibilidad para iPhone. Los ids de anuncio son los de prueba de Google y no sirven para publicar.
- Que las alertas llegan sin que la persona haya aceptado las notificaciones del sistema.

---

## Material que se puede adjuntar

En el repo **no hay capturas de pantalla** ni un PDF del proyecto. Para el diseño hay que sacarlas del teléfono con los datos de ensayo (tema oscuro y, si se puede, una en claro).

| Material | Dónde está |
| --- | --- |
| Logo vectorial (monograma, hexágono, barras cian e índigo, fondo navy) | `app/src/main/res/drawable/ic_logo.xml` |
| Icono de launcher | `app/src/main/res/mipmap-*/ic_launcher.webp` y `ic_launcher_round.webp` |
| Tokens, radios, voz | `DESIGN_SYSTEM.md`, `GLOSARIO_UI.md` |
| Antes/después del rediseño (texto, no imágenes) | `MAQUETA_AQUA.md`, `AUDITORIA_VISUAL.md` |
| Descripción técnica | `README.md` |
| Página del QR de descarga | `ponencia/index.html` (el QR se genera al correr `presentar.bat`) |
| Código | https://github.com/Santiagomunozma/My_Car |
| Este briefing | `CONTEXTO_PONENCIA.md` |

Paleta para las diapositivas, si se quiere la misma identidad:

- Cian `#38BDF8`
- Índigo `#818CF8`
- Tinta `#0B1120` o `#0F172A`
- Texto claro `#F1F5F9` / `#F8FAFC`
- Éxito `#065F46` sobre `#D1FAE5`
- Advertencia `#92400E` sobre `#FEF3C7`
- Error `#9F1239` sobre `#FFE4E6`
- Degradado del botón: 135° de cian a índigo. En claro, el texto sobre ese degradado es `#0B1120`, no blanco.

Estados que las capturas deberían enseñar, porque el color nunca va solo (siempre icono + texto):

- Documento: Vigente, Por vencer, Vencido.
- Actividad: Al día, Próxima, Vencida, Sin programar, Pausado.

---

## Mensaje de una frase

My Car es el cuaderno del vehículo en el teléfono: te avisa cuando se vence el SOAT o el aceite, guarda lo que hizo el taller y, con Premium o con un mes de prueba, te deja preguntarle al asistente usando ese historial, sin crear una cuenta.

---

## Huecos para completar a mano

Sin esto, quien diseñe las diapositivas no debe inventar:

- [ ] Duración (minutos) y si hay ronda de preguntas.
- [ ] Público: profesores, jurado, compañeros, inversionistas u otro.
- [ ] Plantilla, número de diapositivas, logo institucional, rúbrica.
- [ ] Nombres del equipo y qué hizo cada persona.
- [ ] Institución, materia y fecha de la sustentación.
- [ ] Si alguien de fuera del equipo ya la probó y qué dijo.
- [ ] Capturas reales (inicio con la franja de anuncio, la misma pantalla ya sin anuncio, documentos, mantenimiento, historial, panel Premium, asistente, icono).
- [ ] Si en la demo habrá Wi-Fi. Hace falta para el anuncio de prueba, para Gemini y para el QR. Sin `GEMINI_API_KEY` en el APK, la lectura del documento y el asistente muestran que falta la clave. El ensayo no enciende la prueba: hay que pulsarla en los tres puntos.
