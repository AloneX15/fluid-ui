# Mixins

Fluid UI necesita mixins porque Fabric API no ofrece eventos para dibujar slots, el ítem del cursor ni el selector del
hotbar. Todos son de cliente (`fluid_ui.client.mixins.json`), usan `@Inject`, `@WrapOperation` y `@WrapMethod` de
MixinExtras (nada de `@Overwrite` ni `@Redirect`) y tienen `defaultRequire: 0`: una inyección sin coincidencias puede
omitirse. Esto no garantiza recuperar cualquier conflicto de firmas o de carga de mixins. Los tests de cliente
comprueban los puntos de render utilizados en los perfiles de compatibilidad documentados.

| Mixin | Objetivo | Motivo | Riesgo de conflicto |
|---|---|---|---|
| `AbstractContainerScreenMixin` | `AbstractContainerScreen#extractContents` (`@Inject` HEAD) | Medir el tiempo del frame para las animaciones. | Muy bajo: solo lee el reloj. |
| | `AbstractContainerScreen#extractSlot` (`@WrapMethod`) | Escalar el ítem bajo el cursor y elevar los iguales al del cursor. Hace `pushMatrix`/`popMatrix` alrededor del método original, que siempre se llama. | Bajo: se encadena con otros `@WrapMethod`/`@Inject`. Un mod que sobrescriba `extractSlot` sin llamar a `super` se queda sin estas animaciones en su pantalla. |
| | `AbstractContainerScreen#extractSlot` → `itemDecorations` (`@WrapOperation`) | Dibujar el destello después de las decoraciones vanilla y antes de las inyecciones AFTER de bordes de primer plano. | Comprobado con ambas posiciones de ItemBorders. |
| | `AbstractContainerScreen#extractCarriedItem` (`@Inject` HEAD) | Velocidad del ratón, balanceo y partículas del ítem en el cursor. | Muy bajo. |
| | `AbstractContainerScreen#extractCarriedItem` → `extractFloatingItem` (`@WrapOperation`) | Girar el ítem del cursor. | Bajo: compatible con otros `@WrapOperation`; MixinExtras también puede envolver llamadas redirigidas; se debe comprobar cada integración. |
| `GuiItemRenderStateMixin` | Constructor de `GuiItemRenderState` (`@Inject` TAIL) | Guardar en cada ítem de la GUI si se creó como destello (intensidad de `ShineContext`, 0 en el resto). | Muy bajo: añade un campo `@Unique` y no cambia nada más. |
| `GuiRendererMixin` | `GuiRenderer#submitBlitFromItemAtlas` → `FilterMode.NEAREST` (`@ModifyExpressionValue`) | Usar `LINEAR` solo para los ítems cuya matriz tiene escala o rotación; sin transformar siguen con `NEAREST`. Afecta también al "pop" vanilla del hotbar y a ítems escalados por otros mods (se ven mejor). Opción `smoothItemScaling`. | Bajo: compatible con otros `@ModifyExpressionValue`. Si otro mod reemplaza ese método, los ítems escalados vuelven a verse como en vanilla. |
| | `GuiRenderer#submitBlitFromItemAtlas` → `RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA` y la constante de color `-1` (`@ModifyExpressionValue`) | Solo para los ítems marcados como destello: pipeline aditivo propio (`fluid_ui:pipeline/item_shine`, shader vanilla) y color gris según la intensidad, para que el brillo ilumine solo los píxeles del ítem. | Bajo: los ítems normales no cambian. Si otro mod reemplaza el método, el destello se ve como una copia normal del ítem (sin brillo). |
| `HudMixin` | `Hud#extractItemHotbar` (26.2+) / `Gui#extractItemHotbar` (26.1.x) → `GuiGraphicsExtractor#blitSprite` (`@WrapOperation`) | Desplazar el sprite `hud/hotbar_selection`. Los demás sprites pasan sin cambios. | Bajo. Mods que redibujan el hotbar entero (p. ej. HUD personalizados) pueden no mostrar el deslizamiento. |
| | `Hud#extractSlot` / `Gui#extractSlot` (`@WrapMethod`) | Escalar el ítem del slot seleccionado del hotbar. Los slots de la mano secundaria no se tocan. | Bajo: se encadena con otros `@WrapMethod`. |
| | `Hud#extractSelectedItemName` / `Gui#extractSelectedItemName` → `textWithBackdrop` (`@WrapOperation`) | Zoom de entrada y salida del nombre del ítem en la mano. Lee `toolHighlightTimer` (`@Shadow`). | Held Item Info redirige esta llamada: MixinExtras encadena su dibujo y Fluid UI calcula el centro del bloque completo mediante una integración aislada. Otros reemplazos del HUD requieren validación. |

## Integraciones y pruebas

`HeldItemInfoCompat` solo se invoca si Fabric Loader detecta `held-item-info`. Usa su API de configuración
(`config` en 1.9.1; `getConfig()` desde 1.9.2) y resuelve una vez los miembros de layout añadidos por su mixin:
lista `tooltip` y cálculo de altura de corazones. La llamada de altura usa un `MethodHandle` sin varargs por frame.
Si esos miembros cambian, se registra el error y se desactiva únicamente `ITEM_NAME_ZOOM` hasta recargar.
No se reconstruyen sus textos ni se sustituye su renderizador.

ItemBorders no necesita otro mixin de producción: los `@WrapMethod` de slots incluyen sus inyecciones de fondo y
primer plano. El destello repite solo `graphics.item`, por lo que no vuelve a ejecutar sus bordes. Se añade antes
de las inyecciones de primer plano para respetar `overItems`.

`fluid_ui.test.mixins.json` pertenece exclusivamente al mod de gametests y no al jar distribuido. Observa las
llamadas de texto, fondo, gradientes e ítems para verificar matrices, orden, geometría y ausencia de duplicados;
permite inyectar un fallo de partículas para comprobar la recuperación de la matriz.
