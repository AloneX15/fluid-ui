# Changelog

## 0.2.1

- Corregido: el mod no mostraba icono (estaba fuera de la ruta que declara `fabric.mod.json`).
- Nueva documentación: guía del mod (`docs/guia.md`) y el icono en el README.

## 0.2.0

- Nuevo: el ítem seleccionado en el hotbar crece con un pequeño rebote y vuelve a su tamaño al cambiar de slot.
- Nuevo: el nombre del ítem en la mano aparece con un zoom de entrada y se encoge mientras se desvanece.
- Corregido: el destello al pasar el ratón se dibujaba sobre todo el slot; ahora solo ilumina los píxeles del ítem.
- Las partículas del ítem en el cursor ahora son estrellitas de cuatro puntas que titilan, en vez de cuadrados.
- Los ítems comunes con brillo de encantamiento (libros, herramientas encantadas…) también sueltan estrellitas, en
  violeta.
- Corregido: el ítem ampliado al pasar el ratón se veía deformado (unas filas de píxeles más gruesas que otras) y la
  animación temblaba. Los ítems escalados o girados ahora se dibujan con filtrado suave.
- Nueva opción `smoothItemScaling` para volver al dibujado píxel a píxel de vanilla.

## 0.1.1

- Corregido: en Modrinth la 0.1.0 tenía como archivo principal el jar de código fuente (`-sources.jar`), y el launcher
  instalaba ese jar en lugar del mod, con el error "Invalid mod id ${id}" al arrancar.
- El jar de código fuente ya no incluye `fabric.mod.json`: si se pone en `mods/` por error, el juego lo ignora.

## 0.1.0

- Primera versión, solo de cliente, para Minecraft 26.1.x, 26.2.x y 26.3.x.
- Selector del hotbar que se desliza entre slots.
- Ítem bajo el cursor que crece con rebote.
- Nuevo: destello diagonal sobre el slot bajo el cursor.
- Balanceo con inercia del ítem en el cursor.
- Estela de partículas del color de la rareza para ítems no comunes en el cursor.
- Ítems del contenedor iguales al del cursor que flotan.
- Configuración en `config/fluid_ui.json` y comando `/fluidui reload`.
