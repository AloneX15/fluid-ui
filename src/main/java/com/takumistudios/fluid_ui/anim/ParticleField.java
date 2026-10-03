package com.takumistudios.fluid_ui.anim;

/**
 * Partículas 2D de la estela del ítem en el cursor. Capacidad fija y arrays planos: ninguna asignación por frame.
 * Si se llena, las partículas nuevas se descartan.
 */
public final class ParticleField {
    private static final float GRAVITY = 30.0F;
    private static final float DRAG = 2.5F;

    private final int capacity;
    private final float[] x;
    private final float[] y;
    private final float[] vx;
    private final float[] vy;
    private final float[] age;
    private final float[] life;
    private final float[] size;
    private final int[] color;
    private int count;
    private int seed;

    public ParticleField(int capacity, int seed) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity debe ser > 0");
        }
        this.capacity = capacity;
        this.x = new float[capacity];
        this.y = new float[capacity];
        this.vx = new float[capacity];
        this.vy = new float[capacity];
        this.age = new float[capacity];
        this.life = new float[capacity];
        this.size = new float[capacity];
        this.color = new int[capacity];
        this.seed = seed == 0 ? 0x2545F491 : seed;
    }

    /** @return {@code false} si no queda sitio */
    public boolean spawn(float px, float py, float pvx, float pvy, float lifetime, float particleSize, int rgb) {
        if (count >= capacity || !(lifetime > 0.0F)) {
            return false;
        }
        int i = count++;
        x[i] = px;
        y[i] = py;
        vx[i] = pvx;
        vy[i] = pvy;
        age[i] = 0.0F;
        life[i] = lifetime;
        size[i] = particleSize;
        color[i] = rgb & 0xFFFFFF;
        return true;
    }

    public void update(float dt) {
        if (dt <= 0.0F) {
            return;
        }
        float drag = (float) Math.exp(-DRAG * dt);
        int i = 0;
        while (i < count) {
            age[i] += dt;
            if (age[i] >= life[i]) {
                removeAt(i);
                continue;
            }
            vx[i] *= drag;
            vy[i] = vy[i] * drag + GRAVITY * dt;
            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
            i++;
        }
    }

    private void removeAt(int i) {
        int last = --count;
        x[i] = x[last];
        y[i] = y[last];
        vx[i] = vx[last];
        vy[i] = vy[last];
        age[i] = age[last];
        life[i] = life[last];
        size[i] = size[last];
        color[i] = color[last];
    }

    public void clear() {
        count = 0;
    }

    /** Número aleatorio en [0, 1) (xorshift: determinista con la misma semilla, útil en los tests). */
    public float random() {
        int s = seed;
        s ^= s << 13;
        s ^= s >>> 17;
        s ^= s << 5;
        seed = s;
        return (s >>> 8) * 0x1.0p-24F;
    }

    public int count() {
        return count;
    }

    public int capacity() {
        return capacity;
    }

    public float x(int i) {
        return x[i];
    }

    public float y(int i) {
        return y[i];
    }

    public float size(int i) {
        return size[i];
    }

    public int rgb(int i) {
        return color[i];
    }

    /** Opacidad 0..1: aparece rápido y se desvanece al final de su vida. */
    public float alpha(int i) {
        float t = age[i] / life[i];
        float fadeIn = Math.min(1.0F, t * 8.0F);
        return fadeIn * (1.0F - Easing.easeInOutSine(t));
    }
}
