package com.takumistudios.fluid_ui.config;

/**
 * Configuración de Fluid UI ({@code config/fluid_ui.json}). Los campos públicos los rellena Gson; {@link #validate()}
 * corrige cualquier valor fuera de rango antes de usarlo.
 */
public final class FluidUIConfig {
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;

    /** Interruptor general: en {@code false} el mod no anima nada. */
    public boolean enabled = true;

    /** El selector del hotbar se desliza al cambiar de slot. */
    public boolean hotbarSelector = true;
    /** Velocidad del deslizamiento (1–40). */
    public float hotbarSpeed = 18.0F;

    /** El ítem bajo el cursor crece con un pequeño rebote. */
    public boolean hoverScale = true;
    /** Escala máxima del ítem bajo el cursor (1.0–1.5). */
    public float hoverScaleAmount = 1.18F;

    /** Destello diagonal sobre el slot bajo el cursor (animación nueva de Fluid UI). */
    public boolean hoverShine = true;
    /** Segundos entre destellos mientras el cursor sigue encima (0.5–10). */
    public float shineInterval = 1.8F;

    /** El ítem en el cursor se balancea al moverlo rápido. */
    public boolean carriedWiggle = true;
    /** Intensidad del balanceo (0–3). */
    public float wiggleStrength = 1.0F;

    /** Estela de partículas del color de la rareza para ítems no comunes en el cursor. */
    public boolean carriedParticles = true;
    /** Cantidad de partículas (0–3). */
    public float particleDensity = 1.0F;

    /** Los ítems del contenedor iguales al del cursor flotan. */
    public boolean matchingFloat = true;

    /**
     * Filtrado suave de los ítems escalados o girados (los del cursor, los ampliados y el "pop" vanilla del hotbar). Sin
     * él, al ampliar un ítem algunas filas de píxeles salen más gruesas que otras y la animación tiembla.
     */
    public boolean smoothItemScaling = true;

    /**
     * Corrige los valores fuera de rango o no numéricos.
     *
     * @return {@code true} si había algo que corregir (hay que reescribir el archivo)
     */
    public boolean validate() {
        FluidUIConfig defaults = new FluidUIConfig();
        boolean changed = version != CURRENT_VERSION;
        version = CURRENT_VERSION;

        float v;
        v = clamp(hotbarSpeed, 1.0F, 40.0F, defaults.hotbarSpeed);
        changed |= v != hotbarSpeed;
        hotbarSpeed = v;

        v = clamp(hoverScaleAmount, 1.0F, 1.5F, defaults.hoverScaleAmount);
        changed |= v != hoverScaleAmount;
        hoverScaleAmount = v;

        v = clamp(shineInterval, 0.5F, 10.0F, defaults.shineInterval);
        changed |= v != shineInterval;
        shineInterval = v;

        v = clamp(wiggleStrength, 0.0F, 3.0F, defaults.wiggleStrength);
        changed |= v != wiggleStrength;
        wiggleStrength = v;

        v = clamp(particleDensity, 0.0F, 3.0F, defaults.particleDensity);
        changed |= v != particleDensity;
        particleDensity = v;
        return changed;
    }

    static float clamp(float value, float min, float max, float fallback) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }
}
