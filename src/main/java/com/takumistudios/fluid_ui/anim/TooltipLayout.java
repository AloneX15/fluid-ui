package com.takumistudios.fluid_ui.anim;

/** Geometry of Held Item Info's text block, before applying Fluid UI's zoom. */
public final class TooltipLayout {
    private TooltipLayout() {
    }

    public static int top(int vanillaY, int lines, int lineHeight, float offsetPerExtraLine,
                          int verticalOffset, int nameSpacing, int healthOffset) {
        return vanillaY - (int) ((lineHeight - offsetPerExtraLine) * (lines - 1))
                - verticalOffset - nameSpacing - healthOffset;
    }

    public static int height(int lines, int lineHeight, int nameSpacing) {
        return lines * lineHeight + nameSpacing;
    }
}
