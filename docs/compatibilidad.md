# Compatibilidad y verificación de Fluid UI 0.3.0

Las integraciones son opcionales. No cambian las opciones de otros mods ni los incluyen en el jar de Fluid UI.

## Versiones fijadas para las pruebas

| Minecraft | Held Item Info | ItemBorders | Iceberg | Prism |
|---|---|---|---|---|
| 26.1.2 | 1.9.1 | 1.3.1 | 1.4.1.2 | 1.1.1 |
| 26.2 | 1.9.2 | 1.3.2 | 1.4.2.2 | 1.1.2 |
| 26.3 | 1.9.3 | 1.3.3 | 1.4.3.1 | 1.1.3 |

Iceberg y Prism son dependencias de ItemBorders. Los IDs de versiones Fabric están fijados en
`stonecutter.properties.toml` para evitar resolver por error jars de Forge o NeoForge.

## Comportamiento

- **Held Item Info:** sus líneas y fondo reciben una sola transformación, centrada en el bloque completo. Se respetan
  nombre oculto, altura de línea, separación del nombre, desplazamiento vertical, fondo y ajuste por corazones.
  `itemNameZoom: false` desactiva ese zoom. No se reconstruye su tooltip ni se sustituye su dibujo.
- **ItemBorders:** el borde recibe la misma escala y elevación que el objeto del inventario o hotbar.
  Se respetan ambos valores de `overItems`. El destello se añade antes del borde de primer plano y no vuelve a
  ejecutar el dibujado de bordes. Los slots vacíos no reciben bordes adicionales.
- Un cambio de objeto reinicia el zoom del nombre aunque el temporizador siga teniendo el mismo valor. Las
  animaciones del hotbar se reinician al desconectar para no conservar el estado del mundo anterior.

La integración de Held Item Info consulta miembros internos de su layout. Si una versión futura cambia esos
miembros, Fluid UI registra el error y desactiva el zoom del nombre; el resto de efectos conserva su configuración.
No se garantiza compatibilidad con futuras versiones ni con pantallas que sustituyan el render de slots de vanilla.
Los rangos de Minecraft declarados en los jars no equivalen a haber probado cada parche de esos rangos.

## Reproducir las pruebas

Para cada destino (`26.1.2`, `26.2`, `26.3`):

```powershell
.\gradlew.bat :26.3:build
.\gradlew.bat :26.3:runClientGameTest -PuiCompat=none
.\gradlew.bat :26.3:runClientGameTest -PuiCompat=held
.\gradlew.bat :26.3:runClientGameTest -PuiCompat=borders
.\gradlew.bat :26.3:runClientGameTest -PuiCompat=both
.\gradlew.bat :26.3:runClientGameTest -PcompatPack
```

`build` incluye JUnit y gametests de servidor. Los perfiles de UI se prueban en cliente; un servidor dedicado no
carga Fluid UI. El perfil `compatPack` conserva Lithium y FerriteCore. Los mismos argumentos sirven para `runClient`.

Las pruebas observan llamadas reales a texto, fondos, gradientes e ítems. Comprueban el centro del bloque de texto,
espaciado, una sola aplicación del zoom, matrices iguales para borde e ítem, orden del borde superior respecto al
destello, flotación y ausencia de duplicados. Incluyen texto con y sin nombre, fondos visibles y ocultos, escalas de
interfaz 2 y 3, corazones adicionales, inventario, cofre y creativo. También fuerzan un fallo de partículas y verifican
que se restaura la matriz y se desactiva solo esa función. Ese error intencionado aparece en el log de pruebas.

Las capturas se generan en `versions/<Minecraft>/build/run/clientGameTest/screenshots/`. En CI, los artifacts se
llaman `screenshots-mc<Minecraft>-<perfil>` y los fallos conservan los logs. No ejecutar `build` ni tests de servidor
durante un test de cliente de la misma versión: Loom puede limpiar su directorio de ejecución.

## Revisión del estándar

Verificación local del 18 de octubre de 2026 (Windows, Java 25): los 15 perfiles de cliente finalizaron correctamente,
con los mods esperados cargados. Las tres builds finales pasaron 29 pruebas unitarias y dos gametests de servidor
cada una. Se revisaron capturas de las tres versiones. La matriz de GitHub Actions está configurada; no se ha
ejecutado ni publicado remotamente durante esta tarea. Los logs y 165 capturas se conservan en `build/compat-results/`.

| Área | Resultado |
|---|---|
| Autoría y empaquetado | TakumiStudios, licencia MIT, icono y metadatos de jar conservados. |
| Plataforma | Se mantiene el alcance aprobado: 26.1.2, 26.2 y 26.3 con Java 25; sin port a 1.21.1. |
| Fiabilidad | Recuperación de configuración y escrituras atómicas existentes; lecturas movidas al executor de E/S y matrices protegidas ante fallos. |
| Rendimiento | Sin E/S en la recarga del hilo de juego; miembros de integración resueltos una vez y estado del nombre copiado solo cuando cambia el objeto. No se ha realizado un benchmark de FPS. |
| Compatibilidad | Perfiles individuales, combinado y de rendimiento; pruebas de geometría y capas, además de capturas. |
| Calidad | Documentación de opciones, mixins, publicación y regresiones actualizada. No se han publicado releases ni subido cambios. |
