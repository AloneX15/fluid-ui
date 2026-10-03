package com.takumistudios.fluid_ui.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.takumistudios.fluid_ui.FluidUI;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.anim.AnimatedScreen;
import com.takumistudios.fluid_ui.client.anim.HotbarAnimations;
import com.takumistudios.fluid_ui.client.anim.ItemSmoothing;
import com.takumistudios.fluid_ui.client.anim.ScreenAnimations;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Test de cliente: abre el inventario y comprueba cada animación (escala y destello al pasar el ratón, balanceo,
 * flotación de ítems iguales, partículas por rareza y selector del hotbar). Guarda capturas en
 * versions/<v>/build/run/clientGameTest/screenshots/. Ejecutar con ./gradlew :26.3:runClientGameTest.
 */
public class FluidUIClientGameTest implements FabricClientGameTest {
    // Índices de InventoryMenu: 9–35 inventario, 36–44 hotbar
    private static final int SLOT_INVENTORY_0 = 9;
    private static final int SLOT_HOTBAR_0 = 36;
    private static final int SLOT_HOTBAR_1 = 37;
    private static final int SLOT_HOTBAR_3 = 39;

    private static @Nullable Screen currentScreen(Minecraft mc) {
        //? if >=26.2 {
        return mc.gui.screen();
        //?} else {
        /*return mc.screen;
        *///?}
    }

    private static void waitForChunks(TestSingleplayerContext world) {
        //? if >=26.2 {
        world.getConnection().waitForChunksRender();
        //?} else {
        /*world.getClientLevel().waitForChunksRender();
        *///?}
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        check(FabricLoader.getInstance().isModLoaded(FluidUI.MOD_ID), "Fluid UI no está cargado en el cliente");

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            waitForChunks(world);
            world.getServer().runCommand("gamemode survival @a");
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("item replace entity @a hotbar.0 with minecraft:diamond 16");
            world.getServer().runCommand("item replace entity @a hotbar.1 with minecraft:enchanted_golden_apple");
            world.getServer().runCommand("item replace entity @a hotbar.2 with minecraft:totem_of_undying");
            world.getServer().runCommand("item replace entity @a inventory.0 with minecraft:diamond 8");
            // Común pero con brillo de encantamiento: también debe soltar estrellitas
            world.getServer().runCommand("item replace entity @a hotbar.3 with minecraft:stick[enchantment_glint_override=true]");
            context.waitTicks(10);

            testInventory(context);
            testHotbar(context);

            for (Feature feature : Feature.values()) {
                check(!feature.broken(), "La animación " + feature + " se desactivó por un error (ver log)");
            }
        }
    }

    private static void testInventory(ClientGameTestContext context) {
        context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
        context.waitForScreen(InventoryScreen.class);
        context.waitTicks(5);

        // Escala y destello sobre la manzana encantada
        // (las capturas a los 2 y 5 ticks caen dentro de la primera pasada del destello, que dura 0.55 s)
        moveToSlot(context, SLOT_HOTBAR_1);
        context.waitTicks(2);
        context.takeScreenshot("fluid_ui_shine_1");
        context.waitTicks(3);
        context.takeScreenshot("fluid_ui_shine_2");
        context.waitTicks(10);
        float hover = animations(context).hoverProgress(SLOT_HOTBAR_1);
        check(hover > 0.9F, "El ítem bajo el cursor no ha crecido (progreso " + hover + ")");
        check(ItemSmoothing.smoothedBlits() > 0, "El ítem ampliado no usa filtrado suave (mixin de GuiRenderer)");
        context.takeScreenshot("fluid_ui_hover");

        // Coger los diamantes: los del inventario deben flotar y el ítem balancearse al moverlo
        moveToSlot(context, SLOT_HOTBAR_0);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(5);
        check(context.computeOnClient(mc -> mc.player.containerMenu.getCarried().is(Items.DIAMOND)),
                "No se han cogido los diamantes con el ratón");
        float maxWiggle = 0.0F;
        // Barrido rápido hacia la derecha por la fila del hotbar; la captura se hace aún en movimiento
        for (int i = 0; i < 5; i++) {
            context.getInput().moveCursor(45, 0);
            context.waitTick();
            maxWiggle = Math.max(maxWiggle, Math.abs(animations(context).wiggleAngle()));
        }
        context.takeScreenshot("fluid_ui_carried");
        check(maxWiggle > 0.01F, "El ítem en el cursor no se ha balanceado (ángulo " + maxWiggle + ")");
        float floating = animations(context).floatProgress(SLOT_INVENTORY_0);
        check(floating > 0.5F, "Los diamantes del inventario no flotan (progreso " + floating + ")");

        // Dejar los diamantes y coger la manzana encantada (no común): estela de partículas
        moveToSlot(context, SLOT_HOTBAR_0);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        moveToSlot(context, SLOT_HOTBAR_1);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        int maxParticles = 0;
        // Vaivén alrededor del slot sin salir de la ventana
        for (int i = 0; i < 10; i++) {
            context.getInput().moveCursor((i / 2) % 2 == 0 ? 40 : -40, i % 2 == 0 ? -12 : 12);
            context.waitTick();
            maxParticles = Math.max(maxParticles, animations(context).particleCount());
        }
        context.takeScreenshot("fluid_ui_particles");
        check(maxParticles > 0, "No hay partículas con un ítem no común en el cursor");
        moveToSlot(context, SLOT_HOTBAR_1);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        checkSparkles(context, SLOT_HOTBAR_3, "un ítem común encantado");

        ScreenAnimations animations = animations(context);
        check(animations.slotCalls() > 0, "El mixin de los slots no se ha aplicado");
        check(animations.carriedCalls() > 0, "El mixin del ítem en el cursor no se ha aplicado");
        context.setScreen(() -> null);
        context.waitTicks(3);
    }

    /** Coge el ítem del slot, lo mueve un rato comprobando que suelta partículas y lo devuelve a su sitio. */
    private static void checkSparkles(ClientGameTestContext context, int slotIndex, String what) {
        moveToSlot(context, slotIndex);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
        int maxParticles = 0;
        for (int i = 0; i < 10; i++) {
            context.getInput().moveCursor((i / 2) % 2 == 0 ? 40 : -40, i % 2 == 0 ? -12 : 12);
            context.waitTick();
            maxParticles = Math.max(maxParticles, animations(context).particleCount());
        }
        check(maxParticles > 0, "No hay partículas con " + what + " en el cursor");
        moveToSlot(context, slotIndex);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.waitTicks(3);
    }

    private static void testHotbar(ClientGameTestContext context) {
        long framesBefore = HotbarAnimations.frames();
        context.getInput().pressKey(options -> options.keyHotbarSlots[8]);
        context.waitTick();
        context.takeScreenshot("fluid_ui_hotbar");
        context.waitTicks(40);
        check(HotbarAnimations.frames() > framesBefore, "El mixin del selector del hotbar no se ha aplicado");
        float position = HotbarAnimations.position();
        check(Math.abs(position - 8.0F) < 0.01F, "El selector no ha llegado al slot 9 (posición " + position + ")");
    }

    private static ScreenAnimations animations(ClientGameTestContext context) {
        return context.computeOnClient(mc -> {
            if (currentScreen(mc) instanceof AnimatedScreen screen) {
                return screen.fluid_ui$animations();
            }
            throw new AssertionError("La pantalla actual no es de inventario: " + currentScreen(mc));
        });
    }

    /** Pone el cursor en el centro de un slot de la pantalla de inventario abierta. */
    private static void moveToSlot(ClientGameTestContext context, int slotIndex) {
        double[] position = context.computeOnClient(mc -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) currentScreen(mc);
            Slot slot = screen.getMenu().slots.get(slotIndex);
            double guiX = intField(screen, "leftPos") + slot.x + 8;
            double guiY = intField(screen, "topPos") + slot.y + 8;
            Window window = mc.getWindow();
            double scaleX = window.getScreenWidth() / (double) window.getGuiScaledWidth();
            double scaleY = window.getScreenHeight() / (double) window.getGuiScaledHeight();
            return new double[]{guiX * scaleX, guiY * scaleY};
        });
        context.getInput().setCursorPos(position[0], position[1]);
    }

    private static int intField(AbstractContainerScreen<?> screen, String name) {
        try {
            Field field = AbstractContainerScreen.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(screen);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("No se pudo leer " + name, e);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
