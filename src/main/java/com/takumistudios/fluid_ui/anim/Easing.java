package com.takumistudios.fluid_ui.anim;

/** Curvas de animación y suavizado independientes de los FPS. Lógica pura, sin Minecraft. */
public final class Easing {
    private Easing() {
    }

    public static float clamp01(float t) {
        return t < 0.0F ? 0.0F : Math.min(t, 1.0F);
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static float easeOutCubic(float t) {
        float u = 1.0F - clamp01(t);
        return 1.0F - u * u * u;
    }

    public static float easeInOutSine(float t) {
        return (float) (-(Math.cos(Math.PI * clamp01(t)) - 1.0) / 2.0);
    }

    /** Sale un poco por encima de 1 antes de asentarse: da el "rebote" al crecer. */
    public static float easeOutBack(float t) {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float u = clamp01(t) - 1.0F;
        return 1.0F + c3 * u * u * u + c1 * u * u;
    }

    /**
     * Acerca {@code current} a {@code target} de forma exponencial. El resultado es el mismo a 30 que a 240 FPS porque
     * depende del tiempo transcurrido y no del número de frames.
     *
     * @param rate cuánto se acerca por segundo (mayor = más rápido)
     * @param dt   segundos desde el frame anterior
     */
    public static float approach(float current, float target, float rate, float dt) {
        if (dt <= 0.0F) {
            return current;
        }
        float factor = 1.0F - (float) Math.exp(-rate * dt);
        float next = current + (target - current) * factor;
        return Math.abs(target - next) < 1.0E-4F ? target : next;
    }
}
