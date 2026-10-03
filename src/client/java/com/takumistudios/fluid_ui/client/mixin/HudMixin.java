package com.takumistudios.fluid_ui.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.anim.HotbarAnimations;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
 * Animaciones del hotbar en el HUD: el selector se desliza, el ítem seleccionado crece y el nombre del ítem en la mano
 * entra y sale con zoom. Ver MIXINS.md.
 */
//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class HudMixin {
    @Unique
    private static final Identifier FLUID_UI$SELECTION = Identifier.withDefaultNamespace("hud/hotbar_selection");
    /** Altura de la línea del nombre: se escala alrededor de su centro. */
    @Unique
    private static final float FLUID_UI$TEXT_HALF_HEIGHT = 4.5F;

    //? if >=26.3 {
    @Unique
    private static final String BLIT_SPRITE = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
    //?} else {
    /*@Unique
    private static final String BLIT_SPRITE = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";
    *///?}

    @Unique
    private static final String TEXT_WITH_BACKDROP = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V";

    @Shadow
    private int toolHighlightTimer;

    /** Desplaza el sprite {@code hud/hotbar_selection}; el resto de sprites pasan sin tocar. */
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

    /** Zoom del ítem del slot seleccionado (los slots de la mano secundaria no se tocan). */
    @WrapMethod(method = "extractSlot")
    private void fluid_ui$zoomSelectedItem(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker,
                                           Player player, ItemStack itemStack, int seed, Operation<Void> original) {
        float scale = 1.0F;
        try {
            // Vanilla: x = centro - 88 + slot * 20 para los 9 slots del hotbar
            int offset = x - (graphics.guiWidth() / 2 - 88);
            if (offset % HotbarAnimations.SLOT_SPACING == 0) {
                int slot = offset / HotbarAnimations.SLOT_SPACING;
                scale = HotbarAnimations.slotScale(slot, player.getInventory().getSelectedSlot(), Util.getNanos());
            }
        } catch (RuntimeException e) {
            Feature.HOTBAR_ITEM_ZOOM.fail(e);
            scale = 1.0F;
        }

        if (scale == 1.0F || itemStack.isEmpty()) {
            original.call(graphics, x, y, deltaTracker, player, itemStack, seed);
            return;
        }
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x + 8.0F, y + 8.0F);
            graphics.pose().scale(scale, scale);
            graphics.pose().translate(-(x + 8.0F), -(y + 8.0F));
            original.call(graphics, x, y, deltaTracker, player, itemStack, seed);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    /** Zoom de entrada y salida del nombre del ítem en la mano, alrededor del centro del texto. */
    @WrapOperation(method = "extractSelectedItemName", at = @At(value = "INVOKE", target = TEXT_WITH_BACKDROP))
    private void fluid_ui$zoomItemName(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y,
                                       int width, int color, Operation<Void> original) {
        float scale = 1.0F;
        try {
            scale = HotbarAnimations.nameScale(toolHighlightTimer, Util.getNanos());
        } catch (RuntimeException e) {
            Feature.ITEM_NAME_ZOOM.fail(e);
            scale = 1.0F;
        }

        if (Math.abs(scale - 1.0F) < 0.002F) {
            original.call(graphics, font, text, x, y, width, color);
            return;
        }
        float cx = x + width / 2.0F;
        float cy = y + FLUID_UI$TEXT_HALF_HEIGHT;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(cx, cy);
            graphics.pose().scale(scale, scale);
            graphics.pose().translate(-cx, -cy);
            original.call(graphics, font, text, x, y, width, color);
        } finally {
            graphics.pose().popMatrix();
        }
    }
}
