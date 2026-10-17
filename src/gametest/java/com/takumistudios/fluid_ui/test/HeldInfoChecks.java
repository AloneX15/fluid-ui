package com.takumistudios.fluid_ui.test;

import com.takumistudios.fluid_ui.client.FluidUIClient;
import com.takumistudios.fluid_ui.client.anim.HotbarAnimations;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

/** Optional test class: not linked when Held Item Info is absent. Config changes stay in memory. */
public final class HeldInfoChecks {
    private HeldInfoChecks() {
    }

    public static void run(ClientGameTestContext context, TestSingleplayerContext world) {
        var saved = context.computeOnClient(mc -> {
            //? if >=26.2 {
            return HeldItemInfo.getConfig();
            //?} else {
            /*return HeldItemInfo.config;
            *///?}
        });
        int savedScale = context.computeOnClient(mc -> mc.options.guiScale().get());
        try {
            world.getServer().runCommand("attribute @p minecraft:max_health base set 60");
            context.getInput().pressKey(options -> options.keyHotbarSlots[0]);
            for (int scenario = 0; scenario < 4; scenario++) {
                int guiScale = scenario % 2 + 2;
                context.runOnClient(mc -> {
                    mc.options.guiScale().set(guiScale);
                    mc.resizeGui();
                });
                boolean name = scenario % 2 == 0;
                boolean background = scenario < 2;
                var config = new HeldItemInfoConfig() {
                    @Override public boolean showName() { return name; }
                    @Override public int lineHeight() { return 11; }
                    @Override public int itemNameSpacing() { return 5; }
                    @Override public int verticalOffset() { return 7; }
                    @Override public TextBackgroundVisiblity tooltipBackgroundVisibility() {
                        return background ? TextBackgroundVisiblity.ALWAYS : TextBackgroundVisiblity.NEVER;
                    }
                };
                context.runOnClient(mc -> RenderChecks.field(HeldItemInfo.class, "config").set(null, config));
                world.getServer().runCommand("item replace entity @a hotbar.0 with minecraft:diamond_sword[custom_name='Compat "
                        + scenario + "',lore=['Compat line one','Compat line two']]");
                context.waitTicks(5);
                context.runOnClient(mc -> {
                    Object hud = RenderChecks.hud(mc);
                    RenderChecks.field(hud.getClass(), "toolHighlightTimer").setInt(hud, 40);
                    HotbarAnimations.reset();
                    var graphics = RenderChecks.graphics(mc);
                    RenderProbe.start();
                    try {
                        RenderChecks.invoke(hud, hud.getClass(), "extractSelectedItemName",
                                new Class<?>[]{GuiGraphicsExtractor.class}, graphics);
                        var lines = RenderProbe.draws.stream().filter(d -> d.kind().equals("text")).toList();
                        var fills = RenderProbe.draws.stream().filter(d -> d.kind().equals("fill")).toList();
                        RenderChecks.check(lines.size() == (name ? 3 : 2), "Held Item Info lines missing or duplicated: " + lines.size());
                        var pose = lines.getFirst().pose();
                        RenderChecks.check(pose.m00() > 0.5F && pose.m00() < 0.9F, "Expected one zoom, got " + pose.m00());
                        RenderChecks.check(Math.abs(pose.m00() - HotbarAnimations.currentNameScale()) < 0.0001F, "Tooltip zoom applied more than once");
                        int height = lines.size() * 11 + (name ? 5 : 0);
                        float cx = graphics.guiWidth() / 2.0F;
                        float cy = lines.getFirst().y() + height / 2.0F;
                        RenderChecks.check(Math.abs(pose.m00() * cx + pose.m20() - cx) < 0.001F, "Tooltip zoom is not horizontally centered");
                        RenderChecks.check(Math.abs(pose.m11() * cy + pose.m21() - cy) < 0.001F, "Tooltip zoom does not use the whole block");
                        for (int i = 0; i < lines.size(); i++) {
                            var line = lines.get(i);
                            RenderChecks.check(line.pose().equals(pose, 0.0001F), "Tooltip lines use different transforms");
                            RenderChecks.check(Math.abs((line.x() + line.right()) / 2.0F - cx) <= 0.5F, "Tooltip line alignment changed");
                            if (i > 0) RenderChecks.check(line.y() - lines.get(i - 1).y() == 11 + (name && i == 1 ? 5 : 0), "Line spacing changed");
                        }
                        RenderChecks.check(fills.size() == (background ? 1 : 0), "Tooltip background visibility changed");
                        if (background) {
                            var fill = fills.getFirst();
                            RenderChecks.check(fill.pose().equals(pose, 0.0001F), "Background is not animated with text");
                            RenderChecks.check(fill.y() == lines.getFirst().y() - 2 && fill.bottom() - fill.y() == height + 4, "Background dimensions changed");
                        }
                        RenderChecks.check(graphics.pose().equals(new Matrix3x2f(), 0.0001F), "Tooltip leaked its transform");
                    } finally {
                        RenderProbe.active = false;
                    }
                });
                context.takeScreenshot("fluid_ui_held_info_" + scenario);
            }
            context.runOnClient(mc -> {
                FluidUIClient.config().itemNameZoom = false;
                Object hud = RenderChecks.hud(mc);
                var graphics = RenderChecks.graphics(mc);
                RenderProbe.start();
                try {
                    RenderChecks.invoke(hud, hud.getClass(), "extractSelectedItemName",
                            new Class<?>[]{GuiGraphicsExtractor.class}, graphics);
                    RenderChecks.check(RenderProbe.draws.stream().allMatch(d -> d.pose().equals(new Matrix3x2f(), 0.0001F)), "Disabled zoom still changes text");
                } finally {
                    RenderProbe.active = false;
                    FluidUIClient.config().itemNameZoom = true;
                }
            });
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Held Item Info rendering check", e);
        } finally {
            context.runOnClient(mc -> {
                try {
                    RenderChecks.field(HeldItemInfo.class, "config").set(null, saved);
                    mc.options.guiScale().set(savedScale);
                    mc.resizeGui();
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("Restore Held Item Info config", e);
                }
            });
            world.getServer().runCommand("attribute @p minecraft:max_health base set 20");
        }
    }
}
