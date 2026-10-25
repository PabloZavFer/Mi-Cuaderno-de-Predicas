# Compilar y publicar

## 1. APK de prueba (para instalar en tu teléfono o compartir con probadores)

En Android Studio: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
Por terminal:

```bash
./gradlew assembleDebug
```

El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo, cópialo al teléfono y
permite «instalar apps de origen desconocido», o usa `adb install app/build/outputs/apk/debug/app-debug.apk`.

## 2. Crear tu clave de subida (una sola vez)

Google Play exige que el AAB esté firmado. **Guarda la clave y sus contraseñas en un lugar seguro**:
si la pierdes tendrás que pedir a Google que la restablezca.

Opción A, desde Android Studio: **Build → Generate Signed App Bundle or APK → Android App Bundle →
Create new…** Esto crea un archivo `.jks`. Android Studio recuerda la clave para las próximas veces.

Opción B, por terminal (con `keytool`, incluido en el JDK):

```bash
keytool -genkeypair -v -keystore ~/claves/cuaderno-upload.jks -alias subida \
  -keyalg RSA -keysize 2048 -validity 10000
```

Para compilar firmado desde la terminal, crea `keystore.properties` en la raíz del proyecto.
Ese archivo ya está en `.gitignore`: **nunca lo subas a GitHub**.

```properties
storeFile=/ruta/completa/cuaderno-upload.jks
storePassword=TU_CONTRASEÑA
keyAlias=subida
keyPassword=TU_CONTRASEÑA
```

## 3. Generar el AAB para Google Play

```bash
./gradlew bundleRelease
```

Resultado: `app/build/outputs/bundle/release/app-release.aab`.
La versión *release* activa R8 (reduce el tamaño). Antes de subirla, instala y prueba una versión release:
**Build → Select Build Variant → release**, o `./gradlew assembleRelease` con la firma configurada.

En cada actualización, sube `versionCode` (1, 2, 3…) y ajusta `versionName` en `app/build.gradle.kts`.

## 4. Antes de la primera publicación

- **applicationId**: hoy es `com.pablozavfer.cuadernopredicas`. Si quieres otro, cámbialo en `app/build.gradle.kts`
  **antes** de publicar; después ya no se puede cambiar.
- **Enlace de donación**: configúralo en `gradle.properties` y revisa [DONACIONES.md](DONACIONES.md).
- **Política de privacidad**: publica el texto de [POLITICA_PRIVACIDAD.md](POLITICA_PRIVACIDAD.md) en una URL
  pública (por ejemplo, GitHub Pages de este repositorio) y pégala en Play Console.

## 5. Pasos en Google Play Console (resumen)

1. Crea la app: nombre «Mi Cuaderno de Prédicas», idioma predeterminado Español, tipo *App*, *Gratis*.
2. **Contenido de la app**:
   - *Política de privacidad*: la URL del paso anterior.
   - *Anuncios*: «No, mi app no contiene anuncios».
   - *Acceso a la app*: «Todas las funciones están disponibles sin restricciones» (no hay inicio de sesión).
   - *Seguridad de los datos*: la app **no recopila ni comparte datos**, no usa internet y guarda todo en el
     dispositivo. Los archivos que el usuario exporta (PDF, JSON) se guardan donde él elige.
   - *Clasificación de contenido*: completa el cuestionario (contenido religioso educativo, sin violencia ni
     compras dentro de la app).
   - *Público objetivo*: adultos (18+, o 13+ si lo prefieres). Elegir menores de 13 activa requisitos de
     Familias, innecesarios aquí.
3. **Ficha de Play Store**: descripción breve y completa, ícono de 512×512, gráfico de 1024×500 y al menos dos
   capturas de pantalla de teléfono.
4. **Pruebas**: si tu cuenta de desarrollador es personal y nueva, Google exige una prueba cerrada con un mínimo
   de probadores durante varios días antes de producción. Revisa el número exacto en Play Console.
5. Sube el AAB a *Prueba interna* primero, instálalo desde Play y repasa [PRUEBAS.md](PRUEBAS.md).

## Mantenimiento económico

- Sin servidores, sin cuentas y sin servicios de pago: el costo es solo la cuota única de desarrollador de Google.
- Google Play exige actualizar `targetSdk` cada año. Cuando Play Console avise, sube `compileSdk` y `targetSdk`
  y acepta las actualizaciones de dependencias que sugiera Android Studio.
- Si cambias la base de datos, sube la versión en `AppDatabase` y escribe una *Migration*.
  Los esquemas se exportan a `app/schemas/`; súbelos al repositorio para tener historial.
