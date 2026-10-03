package com.takumistudios.fluid_ui.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.anim.ItemSmoothing;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.3 {
import com.mojang.renderpearl.api.textures.FilterMode;
//?} else {
/*import com.mojang.blaze3d.textures.FilterMode;
*///?}

/**
 * Los ítems de la GUI se dibujan una vez en un atlas a tamaño exacto y se copian con filtro NEAREST. Si el ítem está
 * escalado o girado, NEAREST engorda unas filas de píxeles y otras no (el ítem se deforma y la animación tiembla); en
 * ese caso se usa LINEAR. Los ítems sin transformar siguen con NEAREST, píxel a píxel como en vanilla. Ver MIXINS.md.
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Unique
    private static final float FLUID_UI$EPSILON = 1.0E-4F;

    //? if >=26.3 {
    @Unique
    private static final String NEAREST = "Lcom/mojang/renderpearl/api/textures/FilterMode;NEAREST:Lcom/mojang/renderpearl/api/textures/FilterMode;";
    //?} else {
    /*@Unique
    private static final String NEAREST = "Lcom/mojang/blaze3d/textures/FilterMode;NEAREST:Lcom/mojang/blaze3d/textures/FilterMode;";
    *///?}

    @ModifyExpressionValue(method = "submitBlitFromItemAtlas", at = @At(value = "FIELD", target = NEAREST))
    private FilterMode fluid_ui$smoothTransformedItems(FilterMode original, @Local(argsOnly = true) GuiItemRenderState itemState) {
        try {
            if (Feature.SMOOTH_ITEM_SCALING.active() && fluid_ui$isTransformed(itemState.pose())) {
                ItemSmoothing.recordSmoothed();
                return FilterMode.LINEAR;
            }
        } catch (RuntimeException e) {
            Feature.SMOOTH_ITEM_SCALING.fail(e);
        }
        return original;
    }

    /** ¿Escala distinta de 1 o rotación? (las traslaciones no cambian el muestreo) */
    @Unique
    private static boolean fluid_ui$isTransformed(Matrix3x2f pose) {
        return Math.abs(pose.m00 - 1.0F) > FLUID_UI$EPSILON || Math.abs(pose.m11 - 1.0F) > FLUID_UI$EPSILON
                || Math.abs(pose.m01) > FLUID_UI$EPSILON || Math.abs(pose.m10) > FLUID_UI$EPSILON;
    }
}
