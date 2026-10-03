package com.takumistudios.fluid_ui.anim;

/** Muelle amortiguado: sigue a un objetivo con algo de inercia y rebote (balanceo del ítem en el cursor). */
public final class Spring {
    /** Paso máximo de integración: estable aunque el juego vaya a pocos FPS. */
    private static final float MAX_STEP = 1.0F / 120.0F;
    /** Un frame enorme (pausa, ventana arrastrada) no debe lanzar el muelle. */
    private static final float MAX_DT = 0.25F;

    private final float stiffness;
    private final float damping;
    private float value;
    private float velocity;

    public Spring(float stiffness, float damping) {
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public float update(float target, float dt) {
        float remaining = Math.min(Math.max(dt, 0.0F), MAX_DT);
        while (remaining > 0.0F) {
            float step = Math.min(remaining, MAX_STEP);
            float acceleration = -stiffness * (value - target) - damping * velocity;
            velocity += acceleration * step;
            value += velocity * step;
            remaining -= step;
        }
        return value;
    }

    public float value() {
        return value;
    }

    public void reset() {
        value = 0.0F;
        velocity = 0.0F;
    }
}
