package com.takumistudios.fluid_ui.client;

import com.takumistudios.fluid_ui.FluidUI;
import com.takumistudios.fluid_ui.config.FluidUIConfig;

/**
 * Cada animación de Fluid UI. Si una lanza una excepción se desactiva solo esa (hasta {@code /fluidui reload}) y el
 * juego sigue funcionando.
 */
public enum Feature {
    HOTBAR_SELECTOR,
    HOVER_SCALE,
    HOVER_SHINE,
    CARRIED_WIGGLE,
    CARRIED_PARTICLES,
    MATCHING_FLOAT;

    private volatile boolean broken;

    /** ¿Está activada en la configuración y sin fallos? */
    public boolean active() {
        FluidUIConfig config = FluidUIClient.config();
        if (broken || !config.enabled) {
            return false;
        }
        return switch (this) {
            case HOTBAR_SELECTOR -> config.hotbarSelector;
            case HOVER_SCALE -> config.hoverScale;
            case HOVER_SHINE -> config.hoverShine;
            case CARRIED_WIGGLE -> config.carriedWiggle;
            case CARRIED_PARTICLES -> config.carriedParticles;
            case MATCHING_FLOAT -> config.matchingFloat;
        };
    }

    public boolean broken() {
        return broken;
    }

    public void fail(Throwable error) {
        if (!broken) {
            broken = true;
            FluidUI.LOGGER.error("Error en la animación {}; se desactiva hasta /fluidui reload", name(), error);
        }
    }

    public static void resetAll() {
        for (Feature feature : values()) {
            feature.broken = false;
        }
    }
}
