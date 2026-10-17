package com.takumistudios.fluid_ui.client.compat;

import com.takumistudios.fluid_ui.anim.TooltipLayout;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

//? if >=26.2 {
import io.github.a5b84.helditeminfo.mixin.HudAccessor;
//?} else {
/*import io.github.a5b84.helditeminfo.mixin.GuiAccessor;
*///?}

/** Loaded only with Held Item Info present. Never rebuilds or replaces its tooltip. */
public final class HeldItemInfoCompat {
    private static Field tooltip;
    private static MethodHandle healthHeight;

    private HeldItemInfoCompat() {
    }

    public static float centerY(Object hud, int vanillaY) throws ReflectiveOperationException {
        if (tooltip == null || healthHeight == null) {
            // @Unique members can acquire a prefix on collision. Resolve once, not per frame.
            for (Field field : hud.getClass().getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())
                        && (field.getName().equals("tooltip") || field.getName().endsWith("$tooltip"))) {
                    field.setAccessible(true);
                    tooltip = field;
                    break;
                }
            }
            if (tooltip == null) {
                throw new NoSuchFieldException("Held Item Info tooltip");
            }
            for (Method method : hud.getClass().getDeclaredMethods()) {
                if (method.getName().endsWith("getHealthBarsTotalHeight")
                        && method.getReturnType() == int.class && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == Player.class) {
                    method.setAccessible(true);
                    healthHeight = MethodHandles.lookup().unreflect(method)
                            .asType(MethodType.methodType(int.class, Object.class, Player.class));
                    break;
                }
            }
            if (healthHeight == null) {
                throw new NoSuchMethodException("Held Item Info getHealthBarsTotalHeight");
            }
        }
        //? if >=26.2 {
        var config = HeldItemInfo.getConfig();
        //?} else {
        /*var config = HeldItemInfo.config;
        *///?}
        int lines = ((List<?>) tooltip.get(hud)).size();
        int spacing = config.showName() && lines > 1 ? config.itemNameSpacing() : 0;
        int healthOffset = 0;
        Minecraft mc = Minecraft.getInstance();
        if (config.preventOverlap() && mc.gameMode != null && mc.gameMode.canHurtPlayer()
                && mc.getCameraEntity() instanceof Player player) {
            //? if >=26.2 {
            int baseline = HudAccessor.getLineHeight();
            //?} else {
            /*int baseline = GuiAccessor.getLineHeight();
            *///?}
            try {
                healthOffset = (int) healthHeight.invokeExact(hud, player) - baseline;
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new ReflectiveOperationException("Held Item Info health layout", e);
            }
        }
        int top = TooltipLayout.top(vanillaY, lines, config.lineHeight(), config.offsetPerExtraLine(),
                config.verticalOffset(), spacing, healthOffset);
        return top + TooltipLayout.height(lines, config.lineHeight(), spacing) / 2.0F;
    }
}
