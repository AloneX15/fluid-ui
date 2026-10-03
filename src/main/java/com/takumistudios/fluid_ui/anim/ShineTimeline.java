package com.takumistudios.fluid_ui.anim;

/**
 * Animación nueva de Fluid UI: un destello diagonal recorre el slot bajo el cursor. Pasa nada más entrar el ratón y se
 * repite cada {@code interval} segundos mientras siga encima.
 */
public final class ShineTimeline {
    /** Duración de una pasada del destello, en segundos. */
    public static final float SWEEP_SECONDS = 0.55F;

    private ShineTimeline() {
    }

    /**
     * @param hoverSeconds segundos que lleva el cursor sobre el slot
     * @param interval     pausa entre pasadas, en segundos
     * @return progreso 0..1 de la pasada actual, o -1 si ahora no hay destello
     */
    public static float progress(float hoverSeconds, float interval) {
        if (hoverSeconds < 0.0F || Float.isNaN(hoverSeconds)) {
            return -1.0F;
        }
        float cycle = SWEEP_SECONDS + Math.max(0.0F, interval);
        float phase = hoverSeconds % cycle;
        return phase < SWEEP_SECONDS ? Easing.easeInOutSine(phase / SWEEP_SECONDS) : -1.0F;
    }

    /** Opacidad de la pasada: entra y sale suave para no aparecer de golpe en el borde. */
    public static float intensity(float progress) {
        return progress < 0.0F ? 0.0F : (float) Math.sin(Math.PI * Easing.clamp01(progress));
    }
}
