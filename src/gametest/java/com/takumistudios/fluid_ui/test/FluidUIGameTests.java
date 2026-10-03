package com.takumistudios.fluid_ui.test;

import com.takumistudios.fluid_ui.FluidUI;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Gametests de servidor: ./gradlew :26.3:runGameTest (y con -PcompatPack en la CI). Fluid UI es solo de cliente, así
 * que lo que se comprueba aquí es que un servidor dedicado arranca y funciona sin cargarlo.
 */
public class FluidUIGameTests {
    @GameTest
    public void notLoadedOnDedicatedServer(GameTestHelper helper) {
        if (FabricLoader.getInstance().isModLoaded(FluidUI.MOD_ID)) {
            throw helper.assertionException("Fluid UI es solo de cliente y no debería cargarse en un servidor dedicado");
        }
        helper.succeed();
    }
}
