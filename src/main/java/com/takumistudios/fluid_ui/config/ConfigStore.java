package com.takumistudios.fluid_ui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.takumistudios.fluid_ui.FluidUI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Lee y escribe {@link FluidUIConfig}. Si el archivo falta se crea con los valores por defecto; si está corrupto se
 * guarda una copia {@code .bak} y se regenera; si tiene valores fuera de rango se corrigen y se reescribe. Las
 * escrituras son atómicas y van en el {@code executor} indicado, nunca en el hilo del juego.
 */
public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private final Executor io;

    public ConfigStore(Path file, Executor io) {
        this.file = file;
        this.io = io;
    }

    public Path file() {
        return file;
    }

    /** Siempre devuelve una configuración válida, aunque el archivo no exista o esté roto. */
    public FluidUIConfig load() {
        if (!Files.isRegularFile(file)) {
            FluidUIConfig defaults = new FluidUIConfig();
            save(defaults);
            return defaults;
        }

        FluidUIConfig config;
        try {
            config = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FluidUIConfig.class);
        } catch (IOException | JsonParseException e) {
            config = null;
            FluidUI.LOGGER.warn("No se pudo leer {}: {}", file, e.getMessage());
        }

        if (config == null) {
            backupCorrupt();
            FluidUIConfig defaults = new FluidUIConfig();
            save(defaults);
            return defaults;
        }
        if (config.validate()) {
            FluidUI.LOGGER.info("{} tenía valores fuera de rango o de otra versión; se han corregido", file.getFileName());
            save(config);
        }
        return config;
    }

    private void backupCorrupt() {
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        try {
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            FluidUI.LOGGER.warn("Configuración corrupta: copia en {} y se regenera con los valores por defecto", backup);
        } catch (IOException e) {
            FluidUI.LOGGER.warn("Configuración corrupta y no se pudo copiar a {}", backup, e);
        }
    }

    /** Serializa en el hilo actual (barato) y escribe en disco en el executor de E/S. */
    public CompletableFuture<Void> save(FluidUIConfig config) {
        String json = GSON.toJson(config);
        return CompletableFuture.runAsync(() -> {
            try {
                writeAtomically(json);
            } catch (IOException e) {
                FluidUI.LOGGER.error("No se pudo guardar {}", file, e);
            }
        }, io);
    }

    private void writeAtomically(String json) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(tmp, json, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
