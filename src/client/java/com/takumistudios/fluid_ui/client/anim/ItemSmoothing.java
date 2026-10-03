package com.takumistudios.fluid_ui.client.anim;

/** Contador de ítems dibujados con filtrado suave (para comprobar en los tests que el mixin se aplicó). */
public final class ItemSmoothing {
    private static long smoothedBlits;

    private ItemSmoothing() {
    }

    public static void recordSmoothed() {
        smoothedBlits++;
    }

    public static long smoothedBlits() {
        return smoothedBlits;
    }
}
