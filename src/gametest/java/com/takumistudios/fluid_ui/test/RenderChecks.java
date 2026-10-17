package com.takumistudios.fluid_ui.test;

import com.takumistudios.fluid_ui.anim.ParticleField;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.anim.ScreenAnimations;
import com.takumistudios.fluid_ui.client.anim.HotbarAnimations;
import com.takumistudios.fluid_ui.client.anim.AnimatedScreen;
import com.takumistudios.fluid_ui.client.render.ShineContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.SimpleContainer;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class RenderChecks {
    private RenderChecks() {
    }

    public static Object hud(Minecraft mc) {
        //? if >=26.2 {
        return mc.gui.hud;
        //?} else {
        /*return mc.gui;
        *///?}
    }

    public static GuiGraphicsExtractor graphics(Minecraft mc) {
        return new GuiGraphicsExtractor(mc, new GuiRenderState(), mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    public static Object invoke(Object owner, Class<?> type, String name, Class<?>[] signature, Object... args) throws ReflectiveOperationException {
        Method method = type.getDeclaredMethod(name, signature);
        method.setAccessible(true);
        return method.invoke(owner, args);
    }

    public static Field field(Class<?> type, String name) throws ReflectiveOperationException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static void borders(Minecraft mc, AbstractContainerScreen<?> screen, Slot slot) throws ReflectiveOperationException {
        Class<?> configType = Class.forName("com.anthonyhilyard.itemborders.config.ItemBordersConfig");
        Object config = configType.getMethod("getInstance").invoke(null);
        Field over = configType.getField("overItems");
        over.setAccessible(true);
        Object saved = over.get(config);
        try {
            for (boolean foreground : new boolean[]{false, true}) {
                over.set(config, (Supplier<Boolean>) () -> foreground);
                var graphics = graphics(mc);
                ScreenAnimations animations = ((AnimatedScreen) screen).fluid_ui$animations();
                field(ScreenAnimations.class, "shineSeconds").setFloat(animations, 0.25F);
                field(ScreenAnimations.class, "dt").setFloat(animations, 0.0F);
                RenderProbe.start();
                try {
                    invoke(screen, AbstractContainerScreen.class, "extractSlot",
                            new Class<?>[]{GuiGraphicsExtractor.class, Slot.class, int.class, int.class}, graphics, slot, 0, 0);
                    assertBorders(graphics, foreground);
                    var shine = RenderProbe.draws.stream().filter(d -> d.kind().equals("item") && d.shine() > 0).toList();
                    check(!shine.isEmpty(), "Expected shine geometry while testing border layering");
                    if (foreground) {
                        int lastShine = RenderProbe.draws.indexOf(shine.getLast());
                        int firstBorder = RenderProbe.draws.indexOf(RenderProbe.draws.stream().filter(d -> d.kind().equals("border")).findFirst().orElseThrow());
                        check(lastShine < firstBorder, "Shine renders over foreground borders");
                    }
                } finally {
                    RenderProbe.active = false;
                }
                ItemStack savedCarried = screen.getMenu().getCarried();
                try {
                    screen.getMenu().setCarried(slot.getItem().copy());
                    field(ScreenAnimations.class, "lastNanos").setLong(animations, Util.getNanos() - 250_000_000L);
                    animations.beginFrame(Util.getNanos());
                    graphics = graphics(mc);
                    RenderProbe.start();
                    invoke(screen, AbstractContainerScreen.class, "extractSlot",
                            new Class<?>[]{GuiGraphicsExtractor.class, Slot.class, int.class, int.class}, graphics, slot, 0, 0);
                    assertBorders(graphics, foreground);
                    check(animations.floatProgress(slot.index) > 0.5F, "Matching item did not float during border check");
                } finally {
                    RenderProbe.active = false;
                    screen.getMenu().setCarried(savedCarried);
                }
                graphics = graphics(mc);
                RenderProbe.start();
                try {
                    invoke(screen, AbstractContainerScreen.class, "extractSlot",
                            new Class<?>[]{GuiGraphicsExtractor.class, Slot.class, int.class, int.class},
                            graphics, new Slot(new SimpleContainer(1), 0, 0, 0), 0, 0);
                    check(RenderProbe.draws.stream().noneMatch(d -> d.kind().equals("border")), "Empty slot received a border");
                } finally {
                    RenderProbe.active = false;
                }
                graphics = graphics(mc);
                RenderProbe.start();
                try {
                    Object hud = hud(mc);
                    int selected = mc.player.getInventory().getSelectedSlot();
                    invoke(hud, hud.getClass(), "extractSlot",
                            new Class<?>[]{GuiGraphicsExtractor.class, int.class, int.class, DeltaTracker.class, Player.class, ItemStack.class, int.class},
                            graphics, graphics.guiWidth() / 2 - 88 + selected * 20, graphics.guiHeight() - 19,
                            DeltaTracker.ZERO, mc.player, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 0);
                    assertBorders(graphics, foreground);
                } finally {
                    RenderProbe.active = false;
                }
            }
        } finally {
            over.set(config, saved);
        }
    }

    private static void assertBorders(GuiGraphicsExtractor graphics, boolean foreground) {
        var items = RenderProbe.draws.stream().filter(d -> d.kind().equals("item") && d.shine() == 0).toList();
        var borders = RenderProbe.draws.stream().filter(d -> d.kind().equals("border")).toList();
        check(items.size() == 1, "Expected one original item, got " + items.size());
        check(borders.size() == 4, "Expected exactly four border edges, got " + borders.size());
        var item = items.getFirst();
        check(item.pose().m00() > 1.01F, "Item zoom was not applied during border check");
        for (var border : borders) {
            check(border.pose().equals(item.pose(), 0.0001F), "Border and item transforms differ");
            check(border.shine() == 0, "Shine leaked into border");
        }
        check((RenderProbe.draws.indexOf(borders.getFirst()) > RenderProbe.draws.indexOf(item)) == foreground,
                "overItems drawing order was not preserved");
        check(graphics.pose().equals(new Matrix3x2f(), 0.0001F), "Slot transform leaked");
        check(ShineContext.current() == 0, "Shine context leaked");
    }

    public static void particleFailure(Minecraft mc) throws ReflectiveOperationException {
        ScreenAnimations animations = new ScreenAnimations();
        ParticleField particles = (ParticleField) field(ScreenAnimations.class, "particles").get(animations);
        particles.spawn(30, 30, 0, 0, 1, 2, 0xFFFFFF);
        particles.update(0.1F);
        var graphics = graphics(mc);
        graphics.pose().translate(17, 23);
        Matrix3x2f before = new Matrix3x2f(graphics.pose());
        RenderProbe.failNextSparkle = true;
        try {
            animations.carriedFrame(graphics, ItemStack.EMPTY, 0, 0);
            check(!RenderProbe.failNextSparkle, "Particle failure was not injected");
            check(Feature.CARRIED_PARTICLES.broken(), "Only the failed feature should be disabled");
            check(graphics.pose().equals(before, 0.0001F), "Particle failure leaked its matrix");
        } finally {
            RenderProbe.failNextSparkle = false;
            Feature.resetAll();
        }
    }

    public static void rapidNameChanges() {
        HotbarAnimations.reset();
        long before = HotbarAnimations.namePops();
        HotbarAnimations.nameScale(40, 1_000_000_000L, new ItemStack(Items.DIAMOND));
        HotbarAnimations.nameScale(40, 1_016_000_000L, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        check(HotbarAnimations.namePops() == before + 2, "Changing items with the same timer did not restart the zoom");
        HotbarAnimations.nameScale(39, 1_032_000_000L, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        check(HotbarAnimations.namePops() == before + 2, "Equivalent item copies restarted the zoom");
        HotbarAnimations.reset();
    }

    public static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
