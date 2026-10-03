# Mixins

Fluid UI necesita mixins porque Fabric API no ofrece eventos para dibujar slots, el ítem del cursor ni el selector del
hotbar. Todos son de cliente (`fluid_ui.client.mixins.json`), usan `@Inject`, `@WrapOperation` y `@WrapMethod` de
MixinExtras (nada de `@Overwrite` ni `@Redirect`) y tienen `defaultRequire: 0`: si otro mod rompe un objetivo, esa
animación deja de funcionar pero el juego arranca. El test de cliente comprueba que cada mixin se aplica.

| Mixin | Objetivo | Motivo | Riesgo de conflicto |
|---|---|---|---|
| `AbstractContainerScreenMixin` | `AbstractContainerScreen#extractContents` (`@Inject` HEAD) | Medir el tiempo del frame para las animaciones. | Muy bajo: solo lee el reloj. |
| | `AbstractContainerScreen#extractSlot` (`@WrapMethod`) | Escalar el ítem bajo el cursor, elevar los iguales al del cursor y dibujar el destello. Hace `pushMatrix`/`popMatrix` alrededor del método original, que siempre se llama. | Bajo: se encadena con otros `@WrapMethod`/`@Inject`. Un mod que sobrescriba `extractSlot` sin llamar a `super` se queda sin estas animaciones en su pantalla. |
| | `AbstractContainerScreen#extractCarriedItem` (`@Inject` HEAD) | Velocidad del ratón, balanceo y partículas del ítem en el cursor. | Muy bajo. |
| | `AbstractContainerScreen#extractCarriedItem` → `extractFloatingItem` (`@WrapOperation`) | Girar el ítem del cursor. | Bajo: compatible con otros `@WrapOperation`; un `@Redirect` de otro mod sobre la misma llamada desactivaría el balanceo. |
| `GuiRendererMixin` | `GuiRenderer#submitBlitFromItemAtlas` → `FilterMode.NEAREST` (`@ModifyExpressionValue`) | Usar `LINEAR` solo para los ítems cuya matriz tiene escala o rotación; sin transformar siguen con `NEAREST`. Afecta también al "pop" vanilla del hotbar y a ítems escalados por otros mods (se ven mejor). Opción `smoothItemScaling`. | Bajo: compatible con otros `@ModifyExpressionValue`. Si otro mod reemplaza ese método, los ítems escalados vuelven a verse como en vanilla. |
| `HotbarSelectorMixin` | `Hud#extractItemHotbar` (26.2+) / `Gui#extractItemHotbar` (26.1.x) → `GuiGraphicsExtractor#blitSprite` (`@WrapOperation`) | Desplazar el sprite `hud/hotbar_selection`. Los demás sprites pasan sin cambios. | Bajo. Mods que redibujan el hotbar entero (p. ej. HUD personalizados) pueden no mostrar el deslizamiento. |
