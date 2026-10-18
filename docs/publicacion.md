# Publicación en Modrinth y CurseForge

Las versiones se publican solas al subir un tag `vX.Y.Z` (workflow `release.yml`, con `Kir-Antipov/mc-publish`). Aquí
están los datos de las fichas y la configuración que necesita el repositorio.

## Datos de las fichas

| Campo | Valor |
|---|---|
| Nombre | Fluid UI |
| Slug / URL | `fluid-ui` |
| Resumen (Modrinth, máx. 256) | Smooth inventory and HUD animations: sliding hotbar, item wiggle, rarity trails, hover zoom and shine. Compatible with Held Item Info, ItemBorders, Lithium and FerriteCore. Client-side only. |
| Licencia | MIT |
| Lado | Cliente: **obligatorio**. Servidor: **no compatible / no necesario** |
| Loader | Fabric (requiere Fabric API) |
| Categorías | Decoration, Utility (CurseForge: Cosmetic, Miscellaneous) |
| Código fuente | https://github.com/AloneX15/fluid-ui |
| Issues | https://github.com/AloneX15/fluid-ui/issues |
| Icono | `src/main/resources/assets/fluid_ui/icon.png` |
| Galería | GIFs `docs/img/demo-*.gif` y capturas optimizadas `docs/img/captura-*.png` (véase «Recursos visuales») |

## Descripción (Markdown, en inglés)

Texto común para las fichas de **Modrinth y CurseForge**. Las compatibilidades indicadas corresponden a las pruebas
documentadas en [Compatibilidad](compatibilidad.md).

```markdown
# Fluid UI

Smooth, lightweight animations for the Minecraft inventory and HUD. **Client-side only**: install it on your game and
play on any server.

## See it in action

### Sliding hotbar and item name zoom

![Sliding hotbar selector and item name zoom](https://raw.githubusercontent.com/AloneX15/fluid-ui/main/docs/img/demo-hotbar.gif)

### Hover zoom and shine

![Hover scaling and shine with ItemBorders](https://raw.githubusercontent.com/AloneX15/fluid-ui/main/docs/img/demo-hover.gif)

### Item wiggle and particle trails

![Carried item wiggle and rarity particle trails](https://raw.githubusercontent.com/AloneX15/fluid-ui/main/docs/img/demo-particulas.gif)

Captured in Minecraft 26.3 with Held Item Info and ItemBorders installed.

### Screenshots

![Inventory with Fluid UI and ItemBorders](https://raw.githubusercontent.com/AloneX15/fluid-ui/main/docs/img/captura-inventario.png)

![Selected hotbar item and animated name](https://raw.githubusercontent.com/AloneX15/fluid-ui/main/docs/img/captura-hotbar.png)

![Hover scaling inside a chest with ItemBorders](https://raw.githubusercontent.com/AloneX15/fluid-ui/main/docs/img/captura-cofre.png)

## Features

- **Sliding hotbar selector**: the selection frame glides between slots instead of jumping.
- **Selected hotbar item zoom**: the selected item grows with a small bounce.
- **Held item text zoom**: the name, or the complete Held Item Info block, animates on entry and fade-out.
- **Hover scaling**: the item under your cursor grows with a little bounce.
- **Hover shine**: a diagonal light sweep crosses the visible pixels of the item you point at, repeating every few seconds.
- **Carried item wiggle**: items held on the cursor tilt with inertia when you move them quickly.
- **Rarity particle trails**: uncommon, rare and epic items leave yellow, aqua or magenta sparkles behind them.
- **Floating matches**: while you carry an item, identical items in the container float up.

All animations are time-based, so they look the same at 30 or 240 FPS. Each one can be turned off in
`config/fluid_ui.json` (reload in game with `/fluidui reload`). Runtime errors in guarded animations disable the affected feature and are recorded in the log.

## Compatibility

Build targets: Minecraft 26.1.2, 26.2 and 26.3, with Fabric Loader and Fabric API.

**Compatible with Held Item Info, ItemBorders, Lithium and FerriteCore** in the tested client profiles.

- **Held Item Info:** keeps its text, spacing and background, animated as a single block.
- **ItemBorders:** borders follow animated items in the inventory and hotbar, whether drawn above or below items.
  Install ItemBorders with its required **Iceberg** and **Prism** dependencies.
- **Lithium and FerriteCore:** tested together in a separate performance profile.

| Minecraft | Held Item Info | ItemBorders |
|---|---|---|
| 26.1.2 | 1.9.1 | 1.3.1 |
| 26.2 | 1.9.2 | 1.3.2 |
| 26.3 | 1.9.3 | 1.3.3 |

All of these mods are optional; none is required to use Fluid UI.

Client tests cover each mod separately, both together, vanilla inventory, chests and creative inventory.
A separate profile covers Lithium and FerriteCore. Compatibility applies to the tested versions; custom screens
that replace vanilla slot rendering need their own validation.

## Credits

Inspired by the ideas of Immersive UI by Octo Studios. Fluid UI is an independent rewrite from scratch for
Minecraft 26.x and contains no code or assets from that mod.

Created by **TakumiStudios**.
```

## Recursos visuales

Vista previa de los GIFs listos para las descripciones:

![Selector del hotbar](img/demo-hotbar.gif)

![Zoom y destello](img/demo-hover.gif)

![Balanceo y partículas](img/demo-particulas.gif)

| Archivo en `docs/img/` | Dimensiones | Peso aproximado | Uso |
|---|---|---|---|
| `demo-hotbar.gif` | 495 × 190 | 94 KiB | Selector y nombre del ítem |
| `demo-hover.gif` | 375 × 335 | 71 KiB | Zoom y destello |
| `demo-particulas.gif` | 450 × 345 | 145 KiB | Balanceo y estela |
| `captura-inventario.png` | 854 × 480 | 59 KiB | Inventario e ItemBorders |
| `captura-hotbar.png` | 854 × 480 | 130 KiB | Hotbar y Held Item Info |
| `captura-cofre.png` | 854 × 480 | 59 KiB | Cofre e ItemBorders |

Los seis archivos suman aproximadamente **556 KiB**. Los GIFs usan una paleta compartida de 128 colores, sin
tramado, fotogramas de 80 ms y compresión de las zonas que cambian. Las capturas PNG conservan sus colores y se
comprimen sin pérdida. Son capturas reales del juego; no se han generado ni simulado los efectos.

La descripción incluye URLs HTTPS absolutas para que las imágenes se carguen fuera de GitHub. Deben existir en
la rama `main` antes de copiar el texto a Modrinth o CurseForge. Si el editor de la plataforma usa texto enriquecido,
inserta las mismas URLs con su herramienta de imagen. Para la galería, usa los PNG; los GIFs se incluyen en la
descripción. Los pesos indicados son objetivos de este proyecto, no límites oficiales de las plataformas.

Para regenerar el material (Java 25 y Python con Pillow):

```powershell
$env:FLUID_UI_CAPTURE = '1'
.\gradlew.bat :26.3:runClientGameTest -PuiCompat=both
Remove-Item Env:FLUID_UI_CAPTURE
py -m pip install Pillow
py scripts/build-media.py
```

## Configuración del repositorio

| Nombre | Tipo | Valor |
|---|---|---|
| `MODRINTH_ID` | variable | ID del proyecto de Modrinth (8 caracteres, en la ficha → menú ⋯ → *Copy ID*) |
| `MODRINTH_TOKEN` | secreto | Token personal de Modrinth con el permiso **Create versions** |
| `CURSEFORGE_ID` | variable | *Project ID* numérico (columna derecha de la ficha de CurseForge) |
| `CURSEFORGE_TOKEN` | secreto | Token de la API de subida de CurseForge |

Sin `MODRINTH_ID` ni `CURSEFORGE_ID` la release solo se publica en GitHub.
