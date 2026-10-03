package com.takumistudios.fluid_ui.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.anim.HotbarAnimations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///?}
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}

/**
 * Desliza el selector del hotbar entre slots. Envuelve el {@code blitSprite} del sprite {@code hud/hotbar_selection}
 * y le añade un desplazamiento horizontal; el resto de sprites pasan sin tocar. Ver MIXINS.md.
 */
//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class HotbarSelectorMixin {
    @Unique
    private static final Identifier FLUID_UI$SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");

    //? if >=26.3 {
    @Unique
    private static final String BLIT_SPRITE = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
    //?} else {
    /*@Unique
    private static final String BLIT_SPRITE = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
    *///?}

    @WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = BLIT_SPRITE))
    private void fluid_ui$slideSelector(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                        int x, int y, int width, int height, Operation<Void> original) {
        if (!FLUID_UI$SELECTION.equals(sprite)) {
            original.call(graphics, pipeline, sprite, x, y, width, height);
            return;
        }

        float offset = 0.0F;
        try {
            // Vanilla: x = centro - 92 + slot * 20
            int selected = Math.floorDiv(x - (graphics.guiWidth() / 2 - 92), HotbarAnimations.SLOT_SPACING);
            offset = HotbarAnimations.selectorOffset(selected, Util.getNanos());
        } catch (RuntimeException e) {
            Feature.HOTBAR_SELECTOR.fail(e);
        }

        if (offset == 0.0F) {
            original.call(graphics, pipeline, sprite, x, y, width, height);
            return;
        }
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(offset, 0.0F);
            original.call(graphics, pipeline, sprite, x, y, width, height);
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
