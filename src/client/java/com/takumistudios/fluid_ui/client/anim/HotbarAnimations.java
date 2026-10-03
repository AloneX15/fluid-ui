package com.takumistudios.fluid_ui.client.anim;

import com.takumistudios.fluid_ui.anim.Easing;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.FluidUIClient;

/** Deslizamiento del selector del hotbar. Solo se usa desde el hilo de render. */
public final class HotbarAnimations {
    /** Separación entre slots del hotbar, en píxeles de GUI. */
    public static final int SLOT_SPACING = 20;
    /** Tras una pausa larga (HUD oculto, menú) el selector salta a su sitio en vez de cruzar la barra. */
    private static final float SNAP_AFTER_SECONDS = 0.5F;

    private static float position = Float.NaN;
    private static long lastNanos;
    private static long frames;

    private HotbarAnimations() {
    }

    /**
     * @param selected slot seleccionado (0–8)
     * @return desplazamiento horizontal del selector respecto a su posición vanilla, en píxeles de GUI
     */
    public static float selectorOffset(int selected, long nanos) {
        frames++;
        float dt = lastNanos == 0L ? Float.MAX_VALUE : (nanos - lastNanos) / 1.0E9F;
        lastNanos = nanos;
        if (!Feature.HOTBAR_SELECTOR.active() || Float.isNaN(position) || dt > SNAP_AFTER_SECONDS) {
            position = selected;
            return 0.0F;
        }
        position = Easing.approach(position, selected, FluidUIClient.config().hotbarSpeed, dt);
        return (position - selected) * SLOT_SPACING;
    }

    /** Posición animada actual en slots (para los tests). */
    public static float position() {
        return position;
    }

    /** Frames en los que se ha dibujado el selector (para comprobar en los tests que el mixin se aplicó). */
    public static long frames() {
        return frames;
    }
}
