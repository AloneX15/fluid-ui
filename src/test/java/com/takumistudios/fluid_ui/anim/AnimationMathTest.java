package com.takumistudios.fluid_ui.anim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimationMathTest {
    @Test
    void easingsStartAtZeroAndEndAtOne() {
        assertEquals(0.0F, Easing.easeOutCubic(0.0F), 1.0E-6F);
        assertEquals(1.0F, Easing.easeOutCubic(1.0F), 1.0E-6F);
        assertEquals(0.0F, Easing.easeInOutSine(0.0F), 1.0E-6F);
        assertEquals(1.0F, Easing.easeInOutSine(1.0F), 1.0E-6F);
        assertEquals(0.0F, Easing.easeOutBack(0.0F), 1.0E-5F);
        assertEquals(1.0F, Easing.easeOutBack(1.0F), 1.0E-5F);
        assertEquals(1.0F, Easing.easeOutCubic(5.0F), 1.0E-6F, "fuera de rango se recorta");
    }

    @Test
    void easeOutBackOvershoots() {
        float max = 0.0F;
        for (int i = 0; i <= 100; i++) {
            max = Math.max(max, Easing.easeOutBack(i / 100.0F));
        }
        assertTrue(max > 1.05F, "debe pasarse de 1 para dar el rebote");
    }

    @Test
    void approachIsFrameRateIndependent() {
        float at30 = 0.0F;
        for (int i = 0; i < 30; i++) {
            at30 = Easing.approach(at30, 1.0F, 10.0F, 1.0F / 30.0F);
        }
        float at240 = 0.0F;
        for (int i = 0; i < 240; i++) {
            at240 = Easing.approach(at240, 1.0F, 10.0F, 1.0F / 240.0F);
        }
        assertEquals(at30, at240, 1.0E-3F);
    }

    @Test
    void approachIgnoresZeroOrNegativeTime() {
        assertEquals(0.3F, Easing.approach(0.3F, 1.0F, 10.0F, 0.0F));
        assertEquals(0.3F, Easing.approach(0.3F, 1.0F, 10.0F, -1.0F));
    }

    @Test
    void approachSnapsWhenClose() {
        float value = 0.0F;
        for (int i = 0; i < 1000; i++) {
            value = Easing.approach(value, 1.0F, 20.0F, 0.016F);
        }
        assertEquals(1.0F, value, "termina exactamente en el objetivo");
    }

    @Test
    void springSettlesOnTarget() {
        Spring spring = new Spring(170.0F, 11.0F);
        for (int i = 0; i < 300; i++) {
            spring.update(0.5F, 1.0F / 60.0F);
        }
        assertEquals(0.5F, spring.value(), 1.0E-3F);
    }

    @Test
    void springOvershootsThenReturns() {
        Spring spring = new Spring(170.0F, 11.0F);
        float max = 0.0F;
        for (int i = 0; i < 120; i++) {
            max = Math.max(max, spring.update(1.0F, 1.0F / 60.0F));
        }
        assertTrue(max > 1.0F, "el balanceo debe rebotar un poco");
        assertTrue(max < 1.5F, "pero sin descontrolarse");
    }

    @Test
    void springSurvivesHugeFrames() {
        Spring spring = new Spring(170.0F, 11.0F);
        float value = spring.update(1.0F, 30.0F);
        assertTrue(Float.isFinite(value));
        assertTrue(Math.abs(value) < 2.0F, "un frame de 30 s (pausa) no lanza el muelle");
    }

    @Test
    void shinePlaysOnHoverAndRepeatsAfterInterval() {
        float interval = 1.0F;
        assertTrue(ShineTimeline.progress(0.0F, interval) >= 0.0F, "empieza nada más entrar el cursor");
        assertTrue(ShineTimeline.progress(ShineTimeline.SWEEP_SECONDS / 2, interval) > 0.0F);
        assertEquals(-1.0F, ShineTimeline.progress(ShineTimeline.SWEEP_SECONDS + 0.5F, interval), "pausa entre pasadas");
        float nextCycle = ShineTimeline.SWEEP_SECONDS + interval + 0.1F;
        assertTrue(ShineTimeline.progress(nextCycle, interval) >= 0.0F, "se repite");
        assertEquals(-1.0F, ShineTimeline.progress(Float.NaN, interval));
        assertEquals(-1.0F, ShineTimeline.progress(-1.0F, interval));
    }

    @Test
    void shineIntensityFadesAtEdges() {
        assertEquals(0.0F, ShineTimeline.intensity(-1.0F));
        assertEquals(0.0F, ShineTimeline.intensity(0.0F), 1.0E-6F);
        assertEquals(1.0F, ShineTimeline.intensity(0.5F), 1.0E-6F);
        assertEquals(0.0F, ShineTimeline.intensity(1.0F), 1.0E-6F);
    }

    @Test
    void particlesRespectCapacityAndExpire() {
        ParticleField field = new ParticleField(4, 1234);
        for (int i = 0; i < 4; i++) {
            assertTrue(field.spawn(0, 0, 10, 0, 0.5F + i * 0.1F, 1, 0xFF0000));
        }
        assertFalse(field.spawn(0, 0, 0, 0, 1, 1, 0xFF0000), "lleno: se descarta");
        assertEquals(4, field.count());

        field.update(0.55F);
        assertEquals(3, field.count(), "la de 0.5 s ha muerto");
        for (int i = 0; i < field.count(); i++) {
            assertTrue(field.x(i) > 0.0F, "se mueven con su velocidad");
            assertTrue(field.y(i) > 0.0F, "y caen con la gravedad");
        }
        field.update(2.0F);
        assertEquals(0, field.count());
    }

    @Test
    void particlesRejectInvalidLifetime() {
        ParticleField field = new ParticleField(2, 1);
        assertFalse(field.spawn(0, 0, 0, 0, 0.0F, 1, 0));
        assertFalse(field.spawn(0, 0, 0, 0, Float.NaN, 1, 0));
        assertEquals(0, field.count());
    }

    @Test
    void particleAlphaFadesOut() {
        ParticleField field = new ParticleField(1, 1);
        field.spawn(0, 0, 0, 0, 1.0F, 1, 0xFFFFFF);
        field.update(0.5F);
        float middle = field.alpha(0);
        field.update(0.45F);
        float end = field.alpha(0);
        assertTrue(middle > end, "se desvanece al final de su vida");
        assertTrue(end >= 0.0F);
    }

    @Test
    void randomIsDeterministicAndInRange() {
        ParticleField a = new ParticleField(1, 42);
        ParticleField b = new ParticleField(1, 42);
        for (int i = 0; i < 1000; i++) {
            float value = a.random();
            assertEquals(value, b.random());
            assertTrue(value >= 0.0F && value < 1.0F);
        }
    }
}
