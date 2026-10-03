package com.takumistudios.fluid_ui.client.anim;

import com.takumistudios.fluid_ui.FluidUI;
import com.takumistudios.fluid_ui.anim.Easing;
import com.takumistudios.fluid_ui.anim.ParticleField;
import com.takumistudios.fluid_ui.anim.ShineTimeline;
import com.takumistudios.fluid_ui.anim.Spring;
import com.takumistudios.fluid_ui.client.Feature;
import com.takumistudios.fluid_ui.client.FluidUIClient;
import com.takumistudios.fluid_ui.config.FluidUIConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Estado de las animaciones de una pantalla de inventario. Hay una instancia por pantalla (campo añadido por el mixin)
 * y solo se usa desde el hilo de render. No asigna memoria por frame: los arrays solo crecen si aparece un slot con un
 * índice mayor.
 */
public final class ScreenAnimations {
    private static final float MAX_DT = 0.25F;
    private static final int SLOT_SIZE = 16;
    /** Píxeles que sube un ítem que coincide con el del cursor (más el vaivén). */
    private static final float FLOAT_LIFT = 1.5F;
    private static final float FLOAT_BOB = 1.5F;
    /** Radianes de giro por cada píxel/segundo de velocidad del ratón. */
    private static final float WIGGLE_PER_SPEED = 0.00045F;
    private static final float WIGGLE_MAX = 0.7F;
    private static final int SHINE_ROWS = SLOT_SIZE * 2;
    /** Violeta del brillo de encantamiento, para ítems comunes encantados. */
    private static final int ENCHANT_COLOR = 0xC98BFF;
    private static final Identifier[] SPARKLES = {
            Identifier.fromNamespaceAndPath(FluidUI.MOD_ID, "sparkle_small"),
            Identifier.fromNamespaceAndPath(FluidUI.MOD_ID, "sparkle_medium"),
            Identifier.fromNamespaceAndPath(FluidUI.MOD_ID, "sparkle_large"),
    };
    private static final int[] SPARKLE_SIZES = {3, 5, 7};

    private long lastNanos;
    private float dt;
    private float time;

    private float[] hover = new float[64];
    private float[] floating = new float[64];

    private @Nullable Slot shineSlot;
    private float shineSeconds;
    private boolean shineSlotSeen;

    private final Spring wiggle = new Spring(170.0F, 11.0F);
    private boolean hasMouse;
    private int lastMouseX;
    private int lastMouseY;
    private float mouseVX;
    private float mouseVY;

    private final ParticleField particles = new ParticleField(160, (int) System.nanoTime());
    private float emitBudget;

    private long frames;
    private long slotCalls;
    private long carriedCalls;

    /** Al principio de cada frame de la pantalla. */
    public void beginFrame(long nanos) {
        dt = lastNanos == 0L ? 0.0F : Math.min((nanos - lastNanos) / 1.0E9F, MAX_DT);
        lastNanos = nanos;
        time += dt;
        frames++;
        if (!shineSlotSeen) {
            shineSlot = null;
        }
        shineSlotSeen = false;
    }

    /**
     * Antes de dibujar un slot: actualiza su estado y, si hace falta, aplica escala o elevación.
     *
     * @return {@code true} si se ha hecho {@code pushMatrix} y hay que hacer {@code popMatrix} después
     */
    public boolean pushSlotTransform(GuiGraphicsExtractor graphics, Slot slot, @Nullable Slot hovered, ItemStack carried) {
        slotCalls++;
        int index = slot.index;
        if (index < 0) {
            return false;
        }
        ensureCapacity(index);
        boolean isHovered = slot == hovered && slot.hasItem();
        float scale = 1.0F;
        float lift = 0.0F;

        if (Feature.HOVER_SCALE.active()) {
            try {
                hover[index] = Easing.approach(hover[index], isHovered ? 1.0F : 0.0F, 14.0F, dt);
                scale = 1.0F + (FluidUIClient.config().hoverScaleAmount - 1.0F) * Easing.easeOutBack(hover[index]);
            } catch (RuntimeException e) {
                Feature.HOVER_SCALE.fail(e);
                scale = 1.0F;
            }
        } else {
            hover[index] = 0.0F;
        }

        if (Feature.MATCHING_FLOAT.active()) {
            try {
                boolean matches = !carried.isEmpty() && slot.hasItem() && ItemStack.isSameItem(slot.getItem(), carried);
                floating[index] = Easing.approach(floating[index], matches ? 1.0F : 0.0F, 10.0F, dt);
                if (floating[index] > 0.0F) {
                    float bob = (float) Math.sin(time * 4.0F + index * 0.7F);
                    lift = Easing.easeOutCubic(floating[index]) * (FLOAT_LIFT + FLOAT_BOB * bob);
                }
            } catch (RuntimeException e) {
                Feature.MATCHING_FLOAT.fail(e);
                lift = 0.0F;
            }
        } else {
            floating[index] = 0.0F;
        }

        if (isHovered) {
            if (slot != shineSlot) {
                shineSlot = slot;
                shineSeconds = 0.0F;
            } else {
                shineSeconds += dt;
            }
            shineSlotSeen = true;
        }

        if (scale == 1.0F && lift == 0.0F) {
            return false;
        }
        float cx = slot.x + SLOT_SIZE / 2.0F;
        float cy = slot.y + SLOT_SIZE / 2.0F;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(cx, cy - lift);
        pose.scale(scale, scale);
        pose.translate(-cx, -cy);
        return true;
    }

    /** Después de dibujar un slot (aún con su transformación): destello sobre el slot bajo el cursor. */
    public void afterSlot(GuiGraphicsExtractor graphics, Slot slot, @Nullable Slot hovered) {
        if (slot != hovered || slot != shineSlot || !slot.hasItem() || !Feature.HOVER_SHINE.active()) {
            return;
        }
        try {
            float progress = ShineTimeline.progress(shineSeconds, FluidUIClient.config().shineInterval);
            if (progress >= 0.0F) {
                drawShine(graphics, slot.x, slot.y, progress);
            }
        } catch (RuntimeException e) {
            Feature.HOVER_SHINE.fail(e);
        }
    }

    /**
     * Banda diagonal blanca que cruza el slot. Se dibuja a media resolución (escala 0.5) para que se mueva con más
     * suavidad que a píxeles enteros de GUI: 32 filas con un halo tenue y un núcleo más brillante.
     */
    private static void drawShine(GuiGraphicsExtractor graphics, int x, int y, float progress) {
        float intensity = ShineTimeline.intensity(progress);
        int glow = ARGB.white((int) (intensity * 50.0F));
        int core = ARGB.white((int) (intensity * 120.0F));
        float base = Easing.lerp(-22.0F, SHINE_ROWS + 4.0F, progress);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(0.5F, 0.5F);
        for (int row = 0; row < SHINE_ROWS; row++) {
            float center = base + (SHINE_ROWS - 1 - row) * 0.6F;
            fillRow(graphics, row, center, 5.0F, glow);
            fillRow(graphics, row, center, 1.8F, core);
        }
        pose.popMatrix();
    }

    private static void fillRow(GuiGraphicsExtractor graphics, int row, float center, float halfWidth, int color) {
        int x0 = Math.max(0, Math.round(center - halfWidth));
        int x1 = Math.min(SHINE_ROWS, Math.round(center + halfWidth));
        if (x1 > x0) {
            graphics.fill(x0, row, x1, row + 1, color);
        }
    }

    /**
     * Una vez por frame, antes del ítem en el cursor: velocidad del ratón, balanceo, emisión y dibujo de partículas.
     * Las partículas siguen vivas un momento después de soltar el ítem.
     */
    public void carriedFrame(GuiGraphicsExtractor graphics, ItemStack carried, int mouseX, int mouseY) {
        carriedCalls++;
        if (dt > 0.0F) {
            if (hasMouse) {
                mouseVX = Easing.approach(mouseVX, (mouseX - lastMouseX) / dt, 18.0F, dt);
                mouseVY = Easing.approach(mouseVY, (mouseY - lastMouseY) / dt, 18.0F, dt);
            }
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            hasMouse = true;
        }

        FluidUIConfig config = FluidUIClient.config();
        if (Feature.CARRIED_WIGGLE.active()) {
            try {
                float target = carried.isEmpty() ? 0.0F
                        : clamp(mouseVX * WIGGLE_PER_SPEED * config.wiggleStrength, -WIGGLE_MAX, WIGGLE_MAX);
                wiggle.update(target, dt);
            } catch (RuntimeException e) {
                Feature.CARRIED_WIGGLE.fail(e);
                wiggle.reset();
            }
        } else {
            wiggle.reset();
        }

        if (Feature.CARRIED_PARTICLES.active()) {
            try {
                emitParticles(carried, mouseX, mouseY, config.particleDensity);
                particles.update(dt);
                drawParticles(graphics);
            } catch (RuntimeException e) {
                Feature.CARRIED_PARTICLES.fail(e);
                particles.clear();
            }
        } else {
            particles.clear();
        }
    }

    private void emitParticles(ItemStack carried, int mouseX, int mouseY, float density) {
        if (carried.isEmpty() || dt <= 0.0F || density <= 0.0F) {
            emitBudget = 0.0F;
            return;
        }
        Rarity rarity = carried.getRarity();
        boolean enchanted = carried.hasFoil();
        if (rarity == Rarity.COMMON && !enchanted) {
            emitBudget = 0.0F;
            return;
        }
        float rarityBoost = switch (rarity) {
            case RARE -> 1.4F;
            case EPIC -> 1.8F;
            default -> 1.0F;
        };
        // Mismos colores que el nombre del ítem (amarillo, aguamarina, magenta); la API de ChatFormatting cambia entre 26.x.
        // Un ítem común con brillo de encantamiento (libro, herramienta…) usa el violeta del encantamiento.
        int rgb = switch (rarity) {
            case RARE -> 0x55FFFF;
            case EPIC -> 0xFF55FF;
            case UNCOMMON -> 0xFFFF55;
            default -> ENCHANT_COLOR;
        };
        float speed = Math.min((float) Math.sqrt(mouseVX * mouseVX + mouseVY * mouseVY), 2000.0F);
        emitBudget += (10.0F + speed * 0.04F) * density * rarityBoost * dt;
        while (emitBudget >= 1.0F) {
            emitBudget -= 1.0F;
            ParticleField p = particles;
            float x = mouseX + (p.random() - 0.5F) * 10.0F;
            float y = mouseY + (p.random() - 0.5F) * 10.0F;
            float vx = -mouseVX * 0.12F + (p.random() - 0.5F) * 16.0F;
            float vy = -mouseVY * 0.12F + (p.random() - 0.5F) * 16.0F - 12.0F;
            float life = 0.5F + p.random() * 0.6F;
            float size = 1.5F + p.random() * 1.5F;
            int color = p.random() < 0.25F ? 0xFFFFFF : rgb;
            if (!p.spawn(x, y, vx, vy, life, size, color)) {
                emitBudget = 0.0F;
                break;
            }
        }
    }

    /**
     * Estrellitas de cuatro puntas: sprites blancos de 7, 5 y 3 píxeles teñidos con el color de la partícula. Se dibujan a
     * media resolución (escala 0.5) y en posiciones enteras, así cada píxel del sprite ocupa píxeles exactos de pantalla
     * y se ven nítidas. El titileo cambia de tamaño (y de brillo) en vez de escalar el sprite.
     */
    private void drawParticles(GuiGraphicsExtractor graphics) {
        int count = particles.count();
        if (count == 0) {
            return;
        }
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(0.5F, 0.5F);
        for (int i = 0; i < count; i++) {
            float twinkle = particles.twinkle(i);
            int alpha = (int) (particles.alpha(i) * (200.0F + 55.0F * twinkle));
            if (alpha <= 0) {
                continue;
            }
            int tier = particles.size(i) > 2.1F ? 2 : particles.size(i) > 1.7F ? 1 : 0;
            if (twinkle < -0.35F && tier > 0) {
                tier--;
            }
            int spriteSize = SPARKLE_SIZES[tier];
            int x = Math.round(particles.x(i) * 2.0F) - spriteSize / 2;
            int y = Math.round(particles.y(i) * 2.0F) - spriteSize / 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPARKLES[tier], x, y, spriteSize, spriteSize,
                    ARGB.color(alpha, particles.rgb(i)));
        }
        pose.popMatrix();
    }

    /**
     * Gira el ítem del cursor según el balanceo.
     *
     * @return {@code true} si se ha hecho {@code pushMatrix}
     */
    public boolean pushCarriedTransform(GuiGraphicsExtractor graphics, int x, int y) {
        float angle = wiggle.value();
        if (!Feature.CARRIED_WIGGLE.active() || Math.abs(angle) < 0.002F) {
            return false;
        }
        float cx = x + SLOT_SIZE / 2.0F;
        float cy = y + SLOT_SIZE / 2.0F;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.rotate(angle);
        pose.translate(-cx, -cy);
        return true;
    }

    private void ensureCapacity(int index) {
        if (index >= hover.length) {
            int size = Math.max(index + 1, hover.length * 2);
            hover = Arrays.copyOf(hover, size);
            floating = Arrays.copyOf(floating, size);
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // Estado expuesto para los tests de cliente

    public float hoverProgress(int slotIndex) {
        return slotIndex >= 0 && slotIndex < hover.length ? hover[slotIndex] : 0.0F;
    }

    public float floatProgress(int slotIndex) {
        return slotIndex >= 0 && slotIndex < floating.length ? floating[slotIndex] : 0.0F;
    }

    public float wiggleAngle() {
        return wiggle.value();
    }

    public int particleCount() {
        return particles.count();
    }

    public long frames() {
        return frames;
    }

    public long slotCalls() {
        return slotCalls;
    }

    public long carriedCalls() {
        return carriedCalls;
    }
}
