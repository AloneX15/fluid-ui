package com.takumistudios.fluid_ui.test.mixin;

import com.takumistudios.fluid_ui.test.RenderProbe;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
*///?}

@Mixin(GuiGraphicsExtractor.class)
public class GraphicsProbeMixin {
    @Inject(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at = @At("HEAD"))
    private void text(Font font, Component text, int x, int y, int color, CallbackInfo ci) {
        RenderProbe.record((GuiGraphicsExtractor) (Object) this, "text", x, y, x + font.width(text), y + 9, text.getString());
    }

    @Inject(method = "fill(IIIII)V", at = @At("HEAD"))
    private void fill(int x, int y, int right, int bottom, int color, CallbackInfo ci) {
        RenderProbe.record((GuiGraphicsExtractor) (Object) this, "fill", x, y, right, bottom, "");
    }

    @Inject(method = "fillGradient(IIIIII)V", at = @At("HEAD"))
    private void gradient(int x, int y, int right, int bottom, int topColor, int bottomColor, CallbackInfo ci) {
        RenderProbe.record((GuiGraphicsExtractor) (Object) this, "border", x, y, right, bottom, "");
    }

    @Inject(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At("HEAD"))
    private void item(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
        RenderProbe.record((GuiGraphicsExtractor) (Object) this, "item", x, y, x + 16, y + 16, stack.getHoverName().getString());
    }

    //? if >=26.3 {
    @Inject(method = "blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V", at = @At("HEAD"))
    //?} else {
    /*@Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V", at = @At("HEAD"))
    *///?}
    private void sparkle(RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, int color, CallbackInfo ci) {
        if (RenderProbe.failNextSparkle && sprite.getNamespace().equals("fluid_ui")) {
            RenderProbe.failNextSparkle = false;
            throw new IllegalStateException("Expected test failure: sparkle rendering");
        }
    }
}
