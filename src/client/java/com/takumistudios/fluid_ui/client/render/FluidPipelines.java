package com.takumistudios.fluid_ui.client.render;

import com.takumistudios.fluid_ui.FluidUI;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
*///?}

/** Pipelines propios. Usan los shaders de vanilla: solo cambia la mezcla de color. */
public final class FluidPipelines {
    /**
     * Como {@code GUI_TEXTURED_PREMULTIPLIED_ALPHA} (el de los ítems de la GUI) pero sumando el color en vez de taparlo:
     * los píxeles del ítem se aclaran y los transparentes no cambian nada.
     */
    public static final RenderPipeline ITEM_SHINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(FluidUI.MOD_ID, "pipeline/item_shine"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .build()
    );

    private FluidPipelines() {
    }

    /** Fuerza la creación y el registro del pipeline al arrancar el cliente. */
    public static void init() {
        // la inicialización estática hace el trabajo
    }
}
