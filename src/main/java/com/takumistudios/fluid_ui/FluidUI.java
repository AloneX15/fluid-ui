package com.takumistudios.fluid_ui;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada común. Fluid UI es solo de cliente ({@code "environment": "client"}): este código nunca se carga en
 * un servidor dedicado. Aquí solo vive la lógica pura (animaciones y configuración); lo que toca la GUI está en el
 * source set {@code client}.
 */
public final class FluidUI implements ModInitializer {
    public static final String MOD_ID = "fluid_ui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Fluid UI {} cargado. Creado por TakumiStudios.", version());
    }

    public static String version() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("desconocida");
    }
}
