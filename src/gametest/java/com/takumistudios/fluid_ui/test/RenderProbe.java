package com.takumistudios.fluid_ui.test;

import com.takumistudios.fluid_ui.client.render.ShineContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

/** Test-only observations at the actual graphics entry points. */
public final class RenderProbe {
    public record Draw(String kind, int x, int y, int right, int bottom, String text,
                       Matrix3x2f pose, float shine) {
    }

    public static final List<Draw> draws = new ArrayList<>();
    public static boolean active;
    public static boolean failNextSparkle;

    private RenderProbe() {
    }

    public static void record(GuiGraphicsExtractor graphics, String kind, int x, int y, int right, int bottom, String text) {
        if (active) {
            draws.add(new Draw(kind, x, y, right, bottom, text, new Matrix3x2f(graphics.pose()), ShineContext.current()));
        }
    }

    public static void start() {
        draws.clear();
        active = true;
    }
}
