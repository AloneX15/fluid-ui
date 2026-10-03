package com.takumistudios.fluid_ui.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.takumistudios.fluid_ui.client.anim.AnimatedScreen;
import com.takumistudios.fluid_ui.client.anim.ScreenAnimations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Animaciones de los slots y del ítem en el cursor en todas las pantallas de inventario. Ver MIXINS.md. Los errores de
 * las animaciones se capturan dentro de {@link ScreenAnimations}: aquí solo se garantiza que cada {@code pushMatrix}
 * tenga su {@code popMatrix} aunque el dibujo vanilla falle.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements AnimatedScreen {
    @Shadow
    protected @Nullable Slot hoveredSlot;

    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Unique
    private final ScreenAnimations fluid_ui$animations = new ScreenAnimations();

    @Override
    public ScreenAnimations fluid_ui$animations() {
        return fluid_ui$animations;
    }

    @Inject(method = "extractContents", at = @At("HEAD"))
    private void fluid_ui$beginFrame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        fluid_ui$animations.beginFrame(Util.getNanos());
    }

    @WrapMethod(method = "extractSlot")
    private void fluid_ui$animateSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
        boolean pushed = fluid_ui$animations.pushSlotTransform(graphics, slot, hoveredSlot, menu.getCarried());
        try {
            original.call(graphics, slot, mouseX, mouseY);
            fluid_ui$animations.afterSlot(graphics, slot, hoveredSlot);
        } finally {
            if (pushed) {
                graphics.pose().popMatrix();
            }
        }
    }

    @Inject(method = "extractCarriedItem", at = @At("HEAD"))
    private void fluid_ui$carriedFrame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        fluid_ui$animations.carriedFrame(graphics, menu.getCarried(), mouseX, mouseY);
    }

    @WrapOperation(
            method = "extractCarriedItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractFloatingItem(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void fluid_ui$wiggleCarried(AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, ItemStack carried,
                                        int x, int y, @Nullable String itemCount, Operation<Void> original) {
        boolean pushed = fluid_ui$animations.pushCarriedTransform(graphics, x, y);
        try {
            original.call(screen, graphics, carried, x, y, itemCount);
        } finally {
            if (pushed) {
                graphics.pose().popMatrix();
            }
        }
    }
}
