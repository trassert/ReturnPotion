package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin; // Убедитесь, что здесь правильный импорт
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final RecallPotionPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(RecallPotionPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString("messages." + path, "&cMessage not found: " + path));
    }

    public String getAdvancementTitle(String advancement) {
        return config.getString("achievements." + advancement + ".title", "Default Title");
    }

    public String getAdvancementDescription(String advancement) {
        return config.getString("achievements." + advancement + ".description", "Default Description");
    }

    public Material getAdvancementIcon(String advancement) {
        String materialName = config.getString("achievements." + advancement + ".icon", "ENDER_EYE");
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material for advancement icon: " + materialName + ". Using ENDER_EYE.");
            return Material.ENDER_EYE;
        }
    }

    public String getAdvancementFrame(String advancement) {
        String frame = config.getString("achievements." + advancement + ".frame", "task");
        if (!frame.equalsIgnoreCase("task") && !frame.equalsIgnoreCase("challenge")
                && !frame.equalsIgnoreCase("goal")) {
            plugin.getLogger().warning("Invalid frame for advancement: " + frame + ". Using 'task'.");
            return "task";
        }
        return frame;
    }

    public String getAdvancementParent(String advancement) {
        return config.getString("achievements." + advancement + ".parent", "minecraft:adventure/root");
    }

    public Sound getSound(String path) {
        try {
            return Sound.valueOf(config.getString("sounds." + path, "BLOCK_NOTE_BLOCK_PLING"));
        } catch (IllegalArgumentException e) {
            return Sound.BLOCK_NOTE_BLOCK_PLING;
        }
    }

    public int getXpCost() {
        return config.getInt("settings.xp-cost", 8);
    }

    public String getPotionName() {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString("settings.potion-name", "&dЗелье возврата"));
    }

    public List<String> getPotionLore() {
        return config.getStringList("settings.potion-lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}
