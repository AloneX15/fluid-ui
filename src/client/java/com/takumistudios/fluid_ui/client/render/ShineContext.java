package com.takumistudios.fluid_ui.client.render;

/**
 * Marca los ítems que se dibujan como "destello". Mientras está activo, cada ítem que se añade a la GUI guarda esta
 * intensidad ({@code GuiItemRenderStateMixin}) y {@code GuiRendererMixin} lo dibuja en modo aditivo: así el brillo solo
 * aclara los píxeles del propio ítem, no el slot. Solo se usa desde el hilo de render.
 */
public final class ShineContext {
    private static float intensity;
    private static long blits;

    private ShineContext() {
    }

    /** Intensidad 0..1 para los ítems que se dibujen a continuación (0 = ítem normal). */
    public static void begin(float value) {
        intensity = Math.max(0.0F, Math.min(1.0F, value));
    }

    public static void end() {
        intensity = 0.0F;
    }

    public static float current() {
        return intensity;
    }

    /** Llamado al dibujar un ítem en modo destello (para comprobar en los tests que el mixin se aplicó). */
    public static void recordBlit() {
        blits++;
    }

    public static long blits() {
        return blits;
    }
}
