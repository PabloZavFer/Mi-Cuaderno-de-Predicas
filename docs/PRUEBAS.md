# Comprobaciones antes de publicar

## Pruebas automáticas

```bash
./gradlew test
```

Cubren lo que no depende de Android (`app/src/test`):

- **Respaldo** (`BackupCodecTest`): exportar y restaurar conserva todo el contenido (acentos, comillas, saltos de
  línea, puntos, etiquetas y fechas). Rechaza archivos vacíos, JSON ajeno, versiones más nuevas, respaldos sin
  prédicas, estados o fechas inválidos e identificadores repetidos, indicando en qué prédica está el problema.
- **Duplicados** (`ImportPlanTest`): las prédicas nuevas se agregan, las idénticas se omiten, las que tienen cambios
  se agregan como copia «(restaurada)» sin tocar la original, y restaurar dos veces no duplica nada.
- **Búsqueda y orden** (`LibraryFilterTest`): la búsqueda ignora mayúsculas y acentos y revisa la referencia y el
  contenido de los puntos. También se prueban los filtros, los dos órdenes y el texto exportado.

## Pruebas manuales (en teléfono o emulador)

Marca cada punto. Haz las pruebas en **modo avión** para confirmar que todo funciona sin conexión.

### Guardado (criterio principal)
- [ ] Primer inicio: aparecen los 3 ejemplos con la etiqueta «Ejemplo editable».
- [ ] «Nueva prédica» → «Prédica expositiva»: los campos traen indicaciones.
- [ ] Escribe un título y un punto. En la barra superior aparece «Guardando…» y luego «✓ Guardado en el dispositivo».
- [ ] Vuelve atrás: la prédica aparece primera en la biblioteca.
- [ ] Cierra la app desde Recientes (deslizar), ábrela de nuevo y comprueba que el contenido sigue ahí.
- [ ] Escribe algo y, en menos de un segundo, pulsa Inicio. Al volver, el cambio está guardado.
- [ ] Abre «Nueva prédica» y sal sin escribir nada: no se crea una prédica vacía.
- [ ] Gira el teléfono mientras editas: no se pierde texto.
- [ ] Puntos: agrega 3, súbelos y bájalos con las flechas y elimina uno con contenido (pide confirmación).
- [ ] Etiquetas: escribe «Fe, Familia» y pulsa la tecla Hecho: se agregan dos etiquetas.
- [ ] Fecha: elige una fecha y quítala con la ✕.

### Biblioteca y búsqueda
- [ ] Busca «pastor» y «PASTOR»: aparece el ejemplo del Salmo 23.
- [ ] Busca «oracion» sin tilde: encuentra textos con «oración».
- [ ] Busca «Hechos 2»: aparece el ejemplo de grupo pequeño (búsqueda por referencia).
- [ ] Busca una palabra que solo esté dentro de un punto: aparece la prédica.
- [ ] Filtra por «Lista» y por una etiqueta. «Quitar filtros» restablece la lista.
- [ ] Ordena por «Fecha de predicación»: primero las próximas, luego las pasadas y al final las que no tienen fecha.
- [ ] Duplicar crea «… (copia)» como borrador y sin fecha.
- [ ] Eliminar pide confirmación; «Cancelar» no borra nada.

### Modo de predicación
- [ ] Botón ▶ del editor o menú «Predicar» de la biblioteca: se oculta la barra de estado y la pantalla no se apaga.
- [ ] «Aa»: A+ y A− cambian el tamaño; el interruptor cambia entre tema claro y oscuro (se recuerda la próxima vez).
- [ ] Temporizador: Iniciar, Pausar, Continuar y Reiniciar.
- [ ] Baja hasta la mitad y gira el teléfono: la posición y el temporizador se conservan.
- [ ] Al salir, la pantalla vuelve a apagarse normalmente.
- [ ] Prueba con *Ajustes del sistema → Tamaño de fuente* al máximo: el texto sigue legible y los botones se pueden usar.

### Calendario
- [ ] Los días con prédica tienen un punto dorado. Al tocar uno aparece su prédica, y al tocarla se abre en el editor.
- [ ] «Nueva prédica para este día» crea una prédica con esa fecha.

### Respaldo y restauración
- [ ] Ajustes → «Exportar respaldo (JSON)» → guarda el archivo (por ejemplo, en Descargas). Se muestra cuántas prédicas se guardaron.
- [ ] Restaura ese mismo archivo: se muestra «Nada que restaurar» (todo es idéntico).
- [ ] Edita una prédica y restaura el respaldo: el resumen indica 1 con cambios. Al confirmar se agrega «… (restaurada)» y tu versión actual sigue intacta.
- [ ] Elimina una prédica y restaura: se recupera.
- [ ] «Cancelar» en el resumen no importa nada.
- [ ] Intenta restaurar un archivo que no sea un respaldo (un PDF o un .txt): aparece un mensaje claro y no se importa nada.
- [ ] **Recuperación completa**: exporta el respaldo, desinstala la app, reinstálala y restaura: vuelven todas las prédicas.

### Exportación
- [ ] Menú ⋮ del editor → «Compartir como texto»: el texto llega completo a WhatsApp o al correo.
- [ ] «Compartir PDF»: se abre el selector y el PDF llega con título, referencia y secciones.
- [ ] «Guardar PDF en el dispositivo»: se guarda donde elijas y se abre en un lector.
- [ ] Con una prédica larga (pega varios párrafos en un punto), el PDF corta las páginas sin solapar texto, ningún
      encabezado queda solo al final de una página y cada página dice «Página X de N».

### Donar
- [ ] Con `donationUrl` vacío: no hay botón y se muestra el mensaje de agradecimiento.
- [ ] Con tu enlace: «Donar» abre el navegador. No aparece ninguna ventana emergente en ninguna parte de la app.
- [ ] Todas las funciones anteriores funcionan sin haber donado.
