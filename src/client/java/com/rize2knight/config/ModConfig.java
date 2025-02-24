package com.rize2knight.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rize2knight.CobblemonRizeTweaksClient;
import org.slf4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private static final Logger LOGGER = CobblemonRizeTweaksClient.INSTANCE.getLOGGER();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/" + CobblemonRizeTweaksClient.MODID + ".json");

    // Use a Map to store dynamic configuration options
    private Map<String, Boolean> configOptions = new HashMap<>();

    private static ModConfig instance;

    public ModConfig() {
        // Initialize default values
        configOptions.put("pc_box_jump", true);
        configOptions.put("hidden_ability_highlighter", true);
        configOptions.put("move_tips", true);
        configOptions.put("type_changes", true);
        configOptions.put("cobblemonuitweaks_pc_scroll_fix", true);
        configOptions.put("cobblemonuitweaks_last_pc_box_fix", true);
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            loadConfig();
        }
        return instance;
    }

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            instance = new ModConfig();
            saveConfig();
            return;
        }

        try (final FileReader reader = new FileReader(CONFIG_FILE)) {
            instance = GSON.fromJson(reader, ModConfig.class);
        } catch (IOException e) {
            LOGGER.warn("Failed to load config file!");
            LOGGER.warn(e.getMessage());
            instance = new ModConfig();
        }
    }

    public static void saveConfig() {
        try (final FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            LOGGER.warn("Failed to save config file!");
            LOGGER.warn(e.getMessage());
        }
    }

    // Method to get a configuration value by key
    public boolean isEnabled(String key) {
        return configOptions.getOrDefault(key, true); // Default to true if key doesn't exist
    }

    // Method to set a configuration value by key
    public void setEnabled(String key, boolean value) {
        configOptions.put(key, value);
        saveConfig(); // Save the config whenever a value is updated
    }

    // Getter for configOptions (used by Gson for serialization)
    public Map<String, Boolean> getConfigOptions() {
        return configOptions;
    }

    // Setter for configOptions (used by Gson for deserialization)
    public void setConfigOptions(Map<String, Boolean> configOptions) {
        this.configOptions = configOptions;
    }
}
