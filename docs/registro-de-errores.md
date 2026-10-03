# Registro de errores

Cada error resuelto: síntoma, causa, solución y test que lo cubre.

| Fecha | Síntoma | Causa | Solución | Test |
|---|---|---|---|---|
| 2026-10-03 | No compila en 26.2: `cannot find symbol ChatFormatting.getColor()`. | La API de `ChatFormatting` cambia entre versiones 26.x. | Colores de rareza fijos en `ScreenAnimations` (los de vanilla: `0xFFFF55`, `0x55FFFF`, `0xFF55FF`), sin depender de `ChatFormatting`. | Compilación de las tres versiones en la CI. |
| 2026-10-03 | El test de cliente no compila en 26.2+: `mc.setScreen` y `mc.screen` no existen. | Desde 26.2 la pantalla actual vive en `Minecraft.gui` (`gui.screen()`, `gui.setScreen(...)`). | `context.setScreen(...)` de Fabric para abrir pantallas y rama `//? if >=26.2` para leer la actual. | `FluidUIClientGameTest`. |
| 2026-10-03 | (Prevención) El hotbar se dibuja en clases distintas según la versión. | 26.1.x lo dibuja en `Gui`; desde 26.2 en `Hud`. Además `RenderPipeline` pasa a `com.mojang.renderpearl.api.pipeline` en 26.3. | `HotbarSelectorMixin` con ramas de Stonecutter para la clase objetivo, el import y el descriptor del `blitSprite`. | `FluidUIClientGameTest#testHotbar` en las tres versiones. |
| 2026-10-03 | (Prevención) Los clics del test de cliente no funcionarían en 26.3 con el botón `0`. | 26.3 usa SDL: el botón izquierdo es `1` y el derecho `3` (antes `0` y `1`). | Usar `InputConstants.MOUSE_BUTTON_LEFT`, que cada versión compila con su valor. | `FluidUIClientGameTest`. |
