package com.takumistudios.fluid_ui.client;

import com.takumistudios.fluid_ui.FluidUI;
import com.takumistudios.fluid_ui.config.ConfigStore;
import com.takumistudios.fluid_ui.config.FluidUIConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Punto de entrada del cliente: configuración y comando {@code /fluidui reload}. Las animaciones están en los mixins. */
public final class FluidUIClient implements ClientModInitializer {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Fluid UI-IO");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile FluidUIConfig config = new FluidUIConfig();
    private static ConfigStore store;

    @Override
    public void onInitializeClient() {
        store = new ConfigStore(FabricLoader.getInstance().getConfigDir().resolve(FluidUI.MOD_ID + ".json"), IO);
        config = store.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommands.literal("fluidui").then(ClientCommands.literal("reload").executes(context -> {
                    try {
                        reload();
                        context.getSource().sendFeedback(Component.translatable("fluid_ui.command.reload.success"));
                        return 1;
                    } catch (RuntimeException e) {
                        FluidUI.LOGGER.error("No se pudo recargar la configuración", e);
                        context.getSource().sendError(Component.translatable("fluid_ui.command.reload.failure"));
                        return 0;
                    }
                }))));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> shutdownIo());
    }

    /** Configuración actual, siempre válida. Se puede leer desde cualquier hilo. */
    public static FluidUIConfig config() {
        return config;
    }

    /** Vuelve a leer el archivo y reactiva las animaciones que se hubieran desactivado por un error. */
    public static void reload() {
        if (store != null) {
            config = store.load();
        }
        Feature.resetAll();
    }

    private static void shutdownIo() {
        IO.shutdown();
        try {
            if (!IO.awaitTermination(5, TimeUnit.SECONDS)) {
                FluidUI.LOGGER.warn("La escritura de la configuración no terminó a tiempo al cerrar el juego");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
