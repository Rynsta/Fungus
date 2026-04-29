package io.github.ryn.fungus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.ryn.fungus.Fungus;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("fungus-viewmodel.json");

    private static final ExecutorService SAVE_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "fungus-config-save");
        t.setDaemon(true);
        return t;
    });

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(PATH);
            ViewmodelConfig cfg = GSON.fromJson(json, ViewmodelConfig.class);
            if (cfg != null) cfg.applyToLive();
        } catch (Exception e) {
            Fungus.LOG.warn("[Fungus] failed to read config, using defaults: {}", e.toString());
        }
    }

    public static void save() {
        ViewmodelConfig cfg = ViewmodelConfig.fromLive();
        SAVE_EXEC.submit(() -> writeAsync(cfg));
    }

    private static void writeAsync(ViewmodelConfig cfg) {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(cfg));
        } catch (IOException e) {
            Fungus.LOG.warn("[Fungus] failed to save config: {}", e.toString());
        }
    }
}
