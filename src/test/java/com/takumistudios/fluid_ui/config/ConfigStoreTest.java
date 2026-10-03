package com.takumistudios.fluid_ui.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigStoreTest {
    @TempDir
    Path dir;

    private ConfigStore store() {
        return new ConfigStore(dir.resolve("fluid_ui.json"), Runnable::run);
    }

    @Test
    void missingFileCreatesDefaults() {
        ConfigStore store = store();
        FluidUIConfig config = store.load();
        assertTrue(config.enabled);
        assertEquals(FluidUIConfig.CURRENT_VERSION, config.version);
        assertTrue(Files.isRegularFile(store.file()));
        assertFalse(Files.exists(dir.resolve("fluid_ui.json.tmp")), "la escritura atómica no deja temporales");
    }

    @Test
    void roundTripKeepsValues() throws IOException {
        ConfigStore store = store();
        FluidUIConfig config = new FluidUIConfig();
        config.hoverShine = false;
        config.shineInterval = 3.5F;
        store.save(config).join();

        FluidUIConfig loaded = store.load();
        assertFalse(loaded.hoverShine);
        assertEquals(3.5F, loaded.shineInterval);
        assertTrue(Files.readString(store.file()).contains("\"shineInterval\""));
    }

    @Test
    void corruptFileIsBackedUpAndRegenerated() throws IOException {
        ConfigStore store = store();
        Files.writeString(store.file(), "{ esto no es json", StandardCharsets.UTF_8);

        FluidUIConfig config = store.load();
        assertTrue(config.enabled);
        Path backup = dir.resolve("fluid_ui.json.bak");
        assertTrue(Files.isRegularFile(backup));
        assertEquals("{ esto no es json", Files.readString(backup));
        assertTrue(Files.readString(store.file()).contains("\"enabled\""));
    }

    @Test
    void emptyFileIsTreatedAsCorrupt() throws IOException {
        ConfigStore store = store();
        Files.writeString(store.file(), "", StandardCharsets.UTF_8);
        assertTrue(store.load().enabled);
        assertTrue(Files.isRegularFile(dir.resolve("fluid_ui.json.bak")));
    }

    @Test
    void outOfRangeValuesAreClampedAndRewritten() throws IOException {
        ConfigStore store = store();
        Files.writeString(store.file(), """
                {"version": 1, "hoverScaleAmount": 9.0, "wiggleStrength": -2, "hotbarSpeed": 0, "particleDensity": 100}
                """, StandardCharsets.UTF_8);

        FluidUIConfig config = store.load();
        assertEquals(1.5F, config.hoverScaleAmount);
        assertEquals(0.0F, config.wiggleStrength);
        assertEquals(1.0F, config.hotbarSpeed);
        assertEquals(3.0F, config.particleDensity);
        assertTrue(Files.readString(store.file()).contains("1.5"), "se reescribe ya corregido");
    }

    @Test
    void oldVersionIsUpgradedKeepingKnownFields() throws IOException {
        ConfigStore store = store();
        Files.writeString(store.file(), "{\"version\": 0, \"carriedWiggle\": false}", StandardCharsets.UTF_8);
        FluidUIConfig config = store.load();
        assertEquals(FluidUIConfig.CURRENT_VERSION, config.version);
        assertFalse(config.carriedWiggle);
        assertTrue(config.hoverShine, "los campos nuevos toman su valor por defecto");
    }

    @Test
    void nonFiniteValuesFallBackToDefaults() {
        FluidUIConfig config = new FluidUIConfig();
        config.shineInterval = Float.NaN;
        config.hotbarSpeed = Float.POSITIVE_INFINITY;
        assertTrue(config.validate());
        assertEquals(new FluidUIConfig().shineInterval, config.shineInterval);
        assertEquals(new FluidUIConfig().hotbarSpeed, config.hotbarSpeed);
    }

    @Test
    void defaultsAreAlreadyValid() {
        assertFalse(new FluidUIConfig().validate());
    }
}
