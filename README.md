# Fluid UI

[![Build](https://github.com/AloneX15/fluid-ui/actions/workflows/build.yml/badge.svg)](https://github.com/AloneX15/fluid-ui/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/AloneX15/fluid-ui?sort=semver)](https://github.com/AloneX15/fluid-ui/releases)
![Minecraft](https://img.shields.io/badge/Minecraft-26.x-62b47a)
![Loader](https://img.shields.io/badge/loader-Fabric-dbd0b4)
![Lado](https://img.shields.io/badge/lado-solo%20cliente-blue)

Animaciones de cliente para el inventario y el HUD: el selector del hotbar se desliza, los ítems se balancean y dejan
una estela del color de su rareza, crecen y brillan bajo el cursor, y los ítems iguales al que llevas flotan.

**Solo de cliente:** instálalo solo en tu juego. Funciona en cualquier servidor (vanilla, Fabric, Paper…) sin que el
servidor lo tenga, y si se pone en un servidor dedicado no se carga ni hace nada.

## Características

- **Selector del hotbar suave:** al cambiar de slot (rueda o números) el marco se desliza en vez de saltar.
- **Escala al pasar el ratón:** el ítem bajo el cursor crece con un pequeño rebote y vuelve a su tamaño al salir. Se
  dibuja con filtrado suave para que no se deforme al ampliarse.
- **Destello al pasar el ratón** *(nuevo en Fluid UI)*: una franja de luz diagonal cruza el slot nada más poner el
  cursor encima y se repite cada pocos segundos mientras sigas ahí.
- **Balanceo del ítem en el cursor:** al moverlo rápido por el inventario se inclina con inercia y rebota al pararse.
- **Estela de estrellitas:** los ítems no comunes o encantados en el cursor sueltan estrellitas que titilan, amarillas
  (poco común), aguamarina (raro), magenta (épico) o violeta (encantado); más cuanto más rápido lo mueves.
- **Ítems iguales que flotan:** mientras llevas un ítem, los del contenedor que son iguales se elevan y oscilan.

Todas las animaciones van por tiempo real, no por frames: se ven igual a 30 que a 240 FPS. Cada una se puede desactivar
por separado y, si alguna fallase por un conflicto con otro mod, se desactiva sola sin afectar al juego.

## Instalación

1. Instala [Fabric Loader](https://fabricmc.net/use/) y [Fabric API](https://modrinth.com/mod/fabric-api).
2. Descarga el jar de tu versión de Minecraft desde [Releases](https://github.com/AloneX15/fluid-ui/releases) y ponlo en `mods/`.

Build de desarrollo (último `main`, ya probado): [release `dev`](https://github.com/AloneX15/fluid-ui/releases/tag/dev).

## Compatibilidad

| Minecraft | Estado |
|---|---|
| 26.1.x | ✅ |
| 26.2.x | ✅ |
| 26.3.x | ✅ |

Probado en la CI junto a Lithium y FerriteCore. Funciona en todas las pantallas con slots (inventario, cofres, hornos,
creativo…) y en las de otros mods que usen las pantallas de contenedor de vanilla. Los mixins están documentados en
[MIXINS.md](MIXINS.md). Si encuentras un conflicto con otro mod, abre un
[issue](https://github.com/AloneX15/fluid-ui/issues).

## Configuración

Archivo `config/fluid_ui.json`, que se crea al arrancar el juego. Tras editarlo, escribe `/fluidui reload` en el chat
para aplicar los cambios sin reiniciar. Todas las opciones y sus rangos están en
[docs/configuracion.md](docs/configuracion.md).

Para jugar sin movimiento en la interfaz (accesibilidad), pon `"enabled": false`.

## Desarrollo

```bash
./gradlew :26.3:build              # compila y ejecuta los tests unitarios
./gradlew :26.3:runGameTest        # gametest de servidor (comprueba que no se carga en servidor)
./gradlew :26.3:runClientGameTest  # test de cliente: prueba cada animación y guarda capturas
./gradlew :26.3:runClient          # abre el juego
```

La lógica de las animaciones (curvas, muelle, partículas, destello) y la configuración están en `src/main` y no
dependen de Minecraft, así que se prueban con JUnit. Lo que dibuja está en `src/client`.

## Créditos

Inspirado en las ideas de [Immersive UI](https://modrinth.com/mod/immersive-ui) de Octo Studios. Fluid UI es una
reimplementación independiente escrita desde cero para Minecraft 26.x: no contiene código ni recursos de ese mod.

## Licencia

[MIT](LICENSE).

---

Creado por **TakumiStudios**.
