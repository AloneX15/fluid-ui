# Configuración

Archivo: `config/fluid_ui.json`. Se crea con los valores por defecto la primera vez que arranca el juego.

- Tras editarlo, `/fluidui reload` en el chat aplica los cambios sin reiniciar. También reactiva las animaciones que se
  hubieran desactivado solas por un error.
- Los valores fuera de rango se corrigen al cargar y el archivo se reescribe ya corregido.
- Si el archivo está roto (JSON inválido), se guarda una copia en `fluid_ui.json.bak` y se regenera con los valores
  por defecto. El log lo indica.

| Opción | Tipo | Por defecto | Rango | Qué hace |
|---|---|---|---|---|
| `version` | número | `1` | — | Versión del formato. No la cambies. |
| `enabled` | sí/no | `true` | — | Interruptor general. En `false` no hay ninguna animación. |
| `hotbarSelector` | sí/no | `true` | — | El selector del hotbar se desliza. |
| `hotbarSpeed` | número | `18.0` | 1–40 | Velocidad del deslizamiento. |
| `hoverScale` | sí/no | `true` | — | El ítem bajo el cursor crece. |
| `hoverScaleAmount` | número | `1.18` | 1.0–1.5 | Tamaño máximo (1.18 = 18 % más grande). |
| `hoverShine` | sí/no | `true` | — | Destello diagonal sobre el slot bajo el cursor. |
| `shineInterval` | número | `1.8` | 0.5–10 | Segundos entre destellos mientras el cursor sigue encima. |
| `carriedWiggle` | sí/no | `true` | — | El ítem del cursor se balancea al moverlo. |
| `wiggleStrength` | número | `1.0` | 0–3 | Intensidad del balanceo. |
| `carriedParticles` | sí/no | `true` | — | Estela de partículas para ítems no comunes en el cursor. |
| `particleDensity` | número | `1.0` | 0–3 | Cantidad de partículas. |
| `matchingFloat` | sí/no | `true` | — | Los ítems iguales al del cursor flotan. |

Ejemplo con menos movimiento:

```json
{
  "version": 1,
  "enabled": true,
  "hotbarSelector": true,
  "hotbarSpeed": 25.0,
  "hoverScale": true,
  "hoverScaleAmount": 1.1,
  "hoverShine": false,
  "shineInterval": 1.8,
  "carriedWiggle": false,
  "wiggleStrength": 1.0,
  "carriedParticles": false,
  "particleDensity": 1.0,
  "matchingFloat": true
}
```

## Comandos

| Comando | Qué hace |
|---|---|
| `/fluidui reload` | Vuelve a leer `config/fluid_ui.json` y reactiva las animaciones desactivadas por errores. Es un comando de cliente: funciona en cualquier servidor y no necesita permisos. |
