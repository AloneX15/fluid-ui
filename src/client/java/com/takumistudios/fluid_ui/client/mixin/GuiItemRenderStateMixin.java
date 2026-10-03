package com.takumistudios.fluid_ui.client.mixin;

import com.takumistudios.fluid_ui.client.render.ShineContext;
import com.takumistudios.fluid_ui.client.render.ShineMarked;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Guarda en cada ítem de la GUI si se creó como destello ({@link ShineContext}). Ver MIXINS.md. */
@Mixin(GuiItemRenderState.class)
public abstract class GuiItemRenderStateMixin implements ShineMarked {
    @Unique
    private float fluid_ui$shine;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluid_ui$markShine(CallbackInfo ci) {
        fluid_ui$shine = ShineContext.current();
    }

    @Override
    public float fluid_ui$shine() {
        return fluid_ui$shine;
    }
}
