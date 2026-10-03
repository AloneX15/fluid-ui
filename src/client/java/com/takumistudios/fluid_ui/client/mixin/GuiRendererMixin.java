package com.takumistudios.fluid_ui.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.anim.ItemSmoothing;
import com.takumistudios.fluid_ui.client.render.FluidPipelines;
import com.takumistudios.fluid_ui.client.render.ShineContext;
import com.takumistudios.fluid_ui.client.render.ShineMarked;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
//?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.FilterMode;
*///?}

/**
 * Ajustes al copiar cada ítem desde el atlas de ítems de la GUI ({@code submitBlitFromItemAtlas}). Ver MIXINS.md.
 * <ul>
 *   <li>Filtrado: el atlas se copia con NEAREST. Si el ítem está escalado o girado, NEAREST engorda unas filas de
 *   píxeles y otras no (el ítem se deforma y la animación tiembla); en ese caso se usa LINEAR.</li>
 *   <li>Destello: los ítems creados como destello ({@link ShineMarked}) se dibujan con un pipeline aditivo y un color
 *   gris según su intensidad, así solo se aclaran los píxeles del ítem.</li>
 * </ul>
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Unique
    private static final float FLUID_UI$EPSILON = 1.0E-4F;

    //? if >=26.3 {
    @Unique
    private static final String NEAREST = "Lcom/mojang/renderpearl/api/textures/FilterMode;NEAREST:Lcom/mojang/renderpearl/api/textures/FilterMode;";
    @Unique
    private static final String ITEM_PIPELINE = "Lnet/minecraft/client/renderer/RenderPipelines;GUI_TEXTURED_PREMULTIPLIED_ALPHA:Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;";
    //?} else {
    /*@Unique
    private static final String NEAREST = "Lcom/mojang/blaze3d/textures/FilterMode;NEAREST:Lcom/mojang/blaze3d/textures/FilterMode;";
    @Unique
    private static final String ITEM_PIPELINE = "Lnet/minecraft/client/renderer/RenderPipelines;GUI_TEXTURED_PREMULTIPLIED_ALPHA:Lcom/mojang/blaze3d/pipeline/RenderPipeline;";
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

    @ModifyExpressionValue(method = "submitBlitFromItemAtlas", at = @At(value = "FIELD", target = ITEM_PIPELINE))
    private RenderPipeline fluid_ui$shinePipeline(RenderPipeline original, @Local(argsOnly = true) GuiItemRenderState itemState) {
        if (fluid_ui$shine(itemState) <= 0.0F) {
            return original;
        }
        ShineContext.recordBlit();
        return FluidPipelines.ITEM_SHINE;
    }

    /** El color del blit (-1 = blanco opaco) pasa a gris según la intensidad: con mezcla aditiva, más gris = más brillo. */
    @ModifyExpressionValue(method = "submitBlitFromItemAtlas", at = @At(value = "CONSTANT", args = "intValue=-1"))
    private int fluid_ui$shineColor(int original, @Local(argsOnly = true) GuiItemRenderState itemState) {
        float shine = fluid_ui$shine(itemState);
        if (shine <= 0.0F) {
            return original;
        }
        int level = Math.round(shine * 255.0F);
        return ARGB.color(255, level, level, level);
    }

    @Unique
    private static float fluid_ui$shine(GuiItemRenderState itemState) {
        // GuiItemRenderState es final: el compilador no sabe que el mixin le añade la interfaz
        return (Object) itemState instanceof ShineMarked marked ? marked.fluid_ui$shine() : 0.0F;
    }

    /** ¿Escala distinta de 1 o rotación? (las traslaciones no cambian el muestreo) */
    @Unique
    private static boolean fluid_ui$isTransformed(Matrix3x2f pose) {
        return Math.abs(pose.m00 - 1.0F) > FLUID_UI$EPSILON || Math.abs(pose.m11 - 1.0F) > FLUID_UI$EPSILON
                || Math.abs(pose.m01) > FLUID_UI$EPSILON || Math.abs(pose.m10) > FLUID_UI$EPSILON;
    }
}
