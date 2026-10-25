# Mi Cuaderno de Prédicas

Aplicación Android gratuita, en español, para preparar, organizar y presentar prédicas desde el celular,
**sin internet y sin cuenta**. Pensada para pastores, predicadores principiantes y líderes de grupos cristianos.

- Sin anuncios, suscripciones, funciones premium ni límites. Todo está disponible para todos.
- Los datos se guardan solo en el dispositivo (Room/SQLite). La app no pide ningún permiso.
- Apartado opcional **Donar**, que abre un enlace externo configurable en el navegador.

## Funciones

| Área | Qué incluye |
|---|---|
| Biblioteca | Crear, editar, duplicar y eliminar (con confirmación). Búsqueda por título, cita y contenido, sin distinguir acentos ni mayúsculas. Filtros por estado (borrador, lista, predicada) y etiqueta. Orden por última modificación o por fecha de predicación. |
| Editor | Título, referencia principal, fecha prevista, contexto, introducción, puntos (agregar, eliminar, subir y bajar), aplicación, conclusión, notas, etiquetas y estado. Guardado automático con indicador «✓ Guardado en el dispositivo». |
| Predicación | Lectura a pantalla completa, letra ajustable, tema claro u oscuro propio, temporizador (iniciar, pausar, reiniciar), pantalla siempre encendida y posición conservada al girar. Permite marcar la prédica como predicada. |
| Calendario | Mes con marcas en los días con prédica. Al tocar un día se ven sus prédicas, que se abren con un toque. Lista de próximas y botón para crear una prédica en ese día. |
| Plantillas | En blanco, expositiva, temática y estudio para grupo pequeño, con indicaciones editables. |
| Respaldo | Exportar todo a JSON. Restaurar con validación, resumen y confirmación. Nunca sobrescribe: los duplicados idénticos se omiten y los que tienen cambios se agregan como copia «(restaurada)». |
| Exportación | Compartir como texto. PDF A4 con título, referencia, secciones y «Página X de N», para compartir o guardar. |
| Ejemplos | Tres prédicas de ejemplo originales, marcadas «Ejemplo editable», que se pueden volver a agregar desde Ajustes. |

## Tecnología

Kotlin 2.2 · Jetpack Compose (BOM 2025.07) · Material 3 · Room 2.7 · Navigation Compose · kotlinx.serialization.
Gradle 8.13 con AGP 8.11 · `minSdk 26` (Android 8.0) · `targetSdk 36`.

La arquitectura es intencionalmente simple: un solo módulo, ViewModels por pantalla, un repositorio y
dependencias creadas a mano en `AppContainer` (sin Hilt).

```
app/src/main/java/com/pablozavfer/cuadernopredicas/
├── CuadernoApp.kt            Application + AppContainer (base de datos, repositorios)
├── MainActivity.kt
├── config/DonationConfig.kt  Lee el enlace de donación (configurado en gradle.properties)
├── data/
│   ├── model/Sermon.kt       Modelo de prédica, puntos y estados
│   ├── local/                Room: entidad, DAO y base de datos
│   ├── backup/               Formato JSON, validación y plan de importación (sin Android)
│   ├── SermonRepository.kt   Acceso a datos
│   ├── SettingsRepository.kt Preferencias (tema, letra)
│   ├── SermonTemplate.kt     Plantillas
│   └── SampleContent.kt      Ejemplos editables
├── export/                   Secciones, texto para compartir, PDF y envío con FileProvider
├── ui/                       Pantallas Compose: library, editor, preach, calendar, donate, settings
└── util/                     Búsqueda sin acentos, fechas en español, lectura y escritura de archivos
```

## Ejecutar en Android Studio

1. Instala una versión reciente y estable de Android Studio (con JDK 17 o superior incluido).
2. **File → Open** y elige la carpeta del proyecto. Espera a que termine *Gradle Sync*.
   Si Android Studio ofrece actualizar AGP o dependencias, puedes aceptar; el proyecto usa versiones estables.
3. Si te pide el SDK 36, instálalo desde **Tools → SDK Manager**.
4. Conecta un teléfono con *Depuración USB* o crea un emulador (**Device Manager**) y pulsa **Run ▶**.

La versión de depuración se instala como `com.pablozavfer.cuadernopredicas.debug`, así que puede convivir
con la versión publicada.

Pruebas unitarias: `./gradlew test`, o clic derecho en `app/src/test` → *Run Tests*.

## Documentación

- [docs/PUBLICACION.md](docs/PUBLICACION.md): APK de prueba, AAB firmado y pasos en Google Play Console.
- [docs/DONACIONES.md](docs/DONACIONES.md): dónde colocar tu enlace y qué verificar de la política de Google Play.
- [docs/PRUEBAS.md](docs/PRUEBAS.md): lista de comprobaciones manuales (guardado, búsqueda, respaldo, exportación…).
- [docs/POLITICA_PRIVACIDAD.md](docs/POLITICA_PRIVACIDAD.md): plantilla de política de privacidad para publicar.

## Licencia del contenido

Los textos de ejemplo y las plantillas son originales de este proyecto. La app no incluye textos bíblicos
de traducciones con derechos de autor: cada usuario escribe sus propias citas y referencias.
