# Apartado «Donar»

## Dónde colocar tu enlace real

Abre `gradle.properties` en la raíz del proyecto y completa la línea:

```properties
donationUrl=https://tu-enlace-real
```

- Debe empezar por `https://`. Si está vacío o no es https, el botón no aparece y la pantalla muestra:
  «Por ahora no hay un enlace de aportación disponible. ¡Gracias por tu interés!».
- Después de cambiarlo, vuelve a compilar (Gradle Sync → Run, o `./gradlew bundleRelease`).
- El valor llega a la app por `BuildConfig.DONATION_URL` y lo lee `config/DonationConfig.kt`.
  No hace falta tocar código.

## Cómo funciona en la app

- Es una sección de la navegación principal. No hay ventanas emergentes ni recordatorios para donar.
- Muestra el texto acordado, un botón principal «Donar» y aclaraciones: el apoyo es voluntario, va dirigido
  al desarrollador y no desbloquea funciones, contenido, insignias ni ventajas.
- Al tocar «Donar» se abre el enlace en el navegador (`Intent.ACTION_VIEW`). La app no procesa pagos,
  no simula transacciones y no pide datos de tarjetas.
- La app no declara el permiso de internet: el navegador es quien abre la página.

## Política de Google Play: verificación pendiente

> **Importante:** no pude abrir la página oficial
> <https://support.google.com/googleplay/android-developer/answer/10281818> desde el entorno donde se creó
> este proyecto. **Léela tú antes de publicar con el enlace activo.**

Lo que conviene comprobar en esa página (sección de pagos) y en la de enlaces externos de Play Console:

1. **Si hay una excepción para aportaciones voluntarias directas al creador** (a veces llamadas propinas o
   *tips* de persona a persona) que no otorgan contenido ni servicios digitales, y en qué condiciones.
   Algunas fuentes secundarias mencionan una condición del tipo «el 100 % va al creador y no se otorga nada a
   cambio». No pude confirmarla con el texto oficial.
2. **Si tu proveedor encaja**: hay casos públicos de apps a las que Google pidió retirar enlaces de donación
   (por ejemplo, AnkiDroid con Open Collective). Un proveedor cuyo dinero va directamente a ti como persona
   encaja mejor con «aportación directa al creador» que una plataforma de financiación de proyectos.
3. **Si tu país o región tiene reglas distintas** para enlaces a pagos externos y si Play Console te pide
   declararlos o inscribirte en algún programa.

Esta implementación ya cumple lo que suele exigirse en estos casos: el pago es opcional, no hay ventajas a
cambio, nada se paga dentro de la app, no hay ventanas emergentes y todas las funciones son gratuitas.

### Plan B, sin cambiar código

Si la revisión de Google rechaza el enlace o tienes dudas, deja `donationUrl=` vacío y vuelve a publicar.
La sección seguirá mostrando el agradecimiento, sin botón ni enlace. Otra alternativa es poner tu enlace en la
descripción de la ficha de Play Store o en tu sitio web.

## Lo que este proyecto NO hace (a propósito)

- No inventa direcciones de pago ni incluye enlaces de ejemplo reales.
- No usa Google Play Billing. Si algún día quisieras aceptar aportaciones dentro de la app, la vía oficial sería
  Play Billing, que requiere cuenta de comerciante y comisiones.
