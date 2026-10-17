package com.takumistudios.fluid_ui.anim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TooltipLayoutTest {
    @Test
    void multilineBlockIncludesNameSpacingAndHealthOffset() {
        assertEquals(59, TooltipLayout.top(100, 3, 9, 1.5F, 4, 2, 20));
        assertEquals(29, TooltipLayout.height(3, 9, 2));
    }

    @Test
    void hiddenNameHasNoExtraSpacing() {
        assertEquals(91, TooltipLayout.top(100, 2, 9, 0, 0, 0, 0));
        assertEquals(18, TooltipLayout.height(2, 9, 0));
    }

    @Test
    void singleLineKeepsVanillaPositionAndEmptyBlockHasNoHeight() {
        assertEquals(100, TooltipLayout.top(100, 1, 9, 0, 0, 0, 0));
        assertEquals(0, TooltipLayout.height(0, 9, 0));
    }
}
