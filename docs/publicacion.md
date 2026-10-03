# Publicación en Modrinth y CurseForge

Las versiones se publican solas al subir un tag `vX.Y.Z` (workflow `release.yml`, con `Kir-Antipov/mc-publish`). Aquí
están los datos de las fichas y la configuración que necesita el repositorio.

## Datos de las fichas

| Campo | Valor |
|---|---|
| Nombre | Fluid UI |
| Slug / URL | `fluid-ui` |
| Resumen (Modrinth, máx. 256) | Client-side animations for your inventory and HUD: sliding hotbar selector, wiggling items with rarity particle trails, hover scaling with a shine sweep and floating matching items. |
| Licencia | MIT |
| Lado | Cliente: **obligatorio**. Servidor: **no compatible / no necesario** |
| Loader | Fabric (requiere Fabric API) |
| Categorías | Decoration, Utility (CurseForge: Cosmetic, Miscellaneous) |
| Código fuente | https://github.com/AloneX15/fluid-ui |
| Issues | https://github.com/AloneX15/fluid-ui/issues |
| Icono | `src/main/resources/assets/fluid_ui/icon.png` |
| Galería | Capturas del test de cliente: artifact `screenshots-mc26.3` de la CI |

## Descripción (Markdown, en inglés)

```markdown
# Fluid UI

Smooth, lightweight animations for the Minecraft inventory and HUD. **Client-side only**: install it on your game and
play on any server.

## Features

- **Sliding hotbar selector**: the selection frame glides between slots instead of jumping.
- **Hover scaling**: the item under your cursor grows with a little bounce.
- **Hover shine** *(new)*: a diagonal light sweep crosses the slot you point at, repeating every few seconds.
- **Carried item wiggle**: items held on the cursor tilt with inertia when you move them quickly.
- **Rarity particle trails**: uncommon, rare and epic items leave yellow, aqua or magenta sparkles behind them.
- **Floating matches**: while you carry an item, identical items in the container float up.

All animations are time-based, so they look the same at 30 or 240 FPS. Each one can be turned off in
`config/fluid_ui.json` (reload in game with `/fluidui reload`). If one of them ever conflicts with another mod, it
disables itself and the game keeps running.

## Compatibility

Minecraft 26.1.x, 26.2.x and 26.3.x with Fabric Loader and Fabric API. Works in every vanilla-style container screen
(inventory, chests, furnaces, creative...). Tested with Lithium and FerriteCore.

## Credits

Inspired by the ideas of Immersive UI by Octo Studios. Fluid UI is an independent rewrite from scratch for
Minecraft 26.x and contains no code or assets from that mod.

Created by **TakumiStudios**.
```

## Configuración del repositorio

| Nombre | Tipo | Valor |
|---|---|---|
| `MODRINTH_ID` | variable | ID del proyecto de Modrinth (8 caracteres, en la ficha → menú ⋯ → *Copy ID*) |
| `MODRINTH_TOKEN` | secreto | Token personal de Modrinth con el permiso **Create versions** |
| `CURSEFORGE_ID` | variable | *Project ID* numérico (columna derecha de la ficha de CurseForge) |
| `CURSEFORGE_TOKEN` | secreto | Token de la API de subida de CurseForge |

Sin `MODRINTH_ID` ni `CURSEFORGE_ID` la release solo se publica en GitHub.
