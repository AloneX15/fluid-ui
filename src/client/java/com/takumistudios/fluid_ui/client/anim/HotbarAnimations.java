package com.takumistudios.fluid_ui.client.anim;

import com.takumistudios.fluid_ui.anim.Easing;
import com.takumistudios.fluid_ui.anim.Spring;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.FluidUIClient;

/**
 * Animaciones del HUD: deslizamiento del selector del hotbar, zoom del ítem seleccionado y zoom de entrada y salida del
 * nombre del ítem en la mano. Solo se usa desde el hilo de render; sin asignaciones por frame.
 */
public final class HotbarAnimations {
    /** Separación entre slots del hotbar, en píxeles de GUI. */
    public static final int SLOT_SPACING = 20;
    public static final int HOTBAR_SLOTS = 9;
    /** Tras una pausa larga (HUD oculto, menú) el selector salta a su sitio en vez de cruzar la barra. */
    private static final float SNAP_AFTER_SECONDS = 0.5F;
    /** Tamaño desde el que aparece el nombre del ítem (zoom de entrada). */
    private static final float NAME_START_SCALE = 0.6F;
    /** Tamaño al que se encoge el nombre mientras se desvanece (zoom de salida). */
    private static final float NAME_END_SCALE = 0.7F;
    /** Ticks finales del nombre en los que vanilla lo desvanece. */
    private static final int NAME_FADE_TICKS = 10;

    private static float position = Float.NaN;
    private static long lastNanos;
    private static long frames;

    private static final float[] SLOT_ZOOM = new float[HOTBAR_SLOTS];
    private static final long[] SLOT_NANOS = new long[HOTBAR_SLOTS];
    private static long slotFrames;

    private static final Spring NAME_SCALE = new Spring(260.0F, 15.0F);
    private static long nameNanos;
    private static int lastNameTimer;
    private static long nameFrames;
    private static long namePops;

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

    /**
     * Zoom del ítem de un slot del hotbar: crece al seleccionarlo y vuelve a su tamaño al cambiar de slot.
     *
     * @return escala del ítem (1 = sin zoom)
     */
    public static float slotScale(int slot, int selected, long nanos) {
        if (slot < 0 || slot >= HOTBAR_SLOTS) {
            return 1.0F;
        }
        slotFrames++;
        float dt = SLOT_NANOS[slot] == 0L ? Float.MAX_VALUE : (nanos - SLOT_NANOS[slot]) / 1.0E9F;
        SLOT_NANOS[slot] = nanos;
        float target = slot == selected ? 1.0F : 0.0F;
        if (!Feature.HOTBAR_ITEM_ZOOM.active()) {
            SLOT_ZOOM[slot] = 0.0F;
            return 1.0F;
        }
        SLOT_ZOOM[slot] = dt > SNAP_AFTER_SECONDS ? target : Easing.approach(SLOT_ZOOM[slot], target, 14.0F, dt);
        return 1.0F + (FluidUIClient.config().hotbarItemScale - 1.0F) * Easing.easeOutBack(SLOT_ZOOM[slot]);
    }

    /**
     * Zoom del nombre del ítem en la mano. Cuando vanilla reinicia su temporizador (ítem nuevo) el nombre arranca
     * pequeño y crece con un rebote; en los últimos ticks, mientras vanilla lo desvanece, se encoge.
     *
     * @param timer {@code toolHighlightTimer} de vanilla, en ticks
     * @return escala del nombre (1 = tamaño normal)
     */
    public static float nameScale(int timer, long nanos) {
        nameFrames++;
        float dt = nameNanos == 0L ? 0.0F : (nanos - nameNanos) / 1.0E9F;
        nameNanos = nanos;
        if (!Feature.ITEM_NAME_ZOOM.active()) {
            NAME_SCALE.snap(1.0F);
            lastNameTimer = timer;
            return 1.0F;
        }
        if (timer > lastNameTimer || dt > SNAP_AFTER_SECONDS) {
            NAME_SCALE.snap(NAME_START_SCALE);
            namePops++;
        }
        lastNameTimer = timer;
        float target = timer < NAME_FADE_TICKS
                ? Easing.lerp(NAME_END_SCALE, 1.0F, Math.max(0, timer) / (float) NAME_FADE_TICKS)
                : 1.0F;
        return NAME_SCALE.update(target, dt);
    }

    // Estado expuesto para los tests de cliente

    /** Posición animada actual del selector, en slots. */
    public static float position() {
        return position;
    }

    /** Frames en los que se ha dibujado el selector (para comprobar que el mixin se aplicó). */
    public static long frames() {
        return frames;
    }

    public static float slotZoom(int slot) {
        return slot >= 0 && slot < HOTBAR_SLOTS ? SLOT_ZOOM[slot] : 0.0F;
    }

    public static long slotFrames() {
        return slotFrames;
    }

    public static float currentNameScale() {
        return NAME_SCALE.value();
    }

    public static long nameFrames() {
        return nameFrames;
    }

    /** Veces que el nombre ha empezado su zoom de entrada. */
    public static long namePops() {
        return namePops;
    }
}
