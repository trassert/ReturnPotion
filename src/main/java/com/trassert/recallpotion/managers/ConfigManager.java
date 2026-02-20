package com.trassert.recallpotion.managers;

import com.trassert.recallpotion.RecallPotionPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final RecallPotionPlugin plugin;
    private FileConfiguration config;

    private static final String DEFAULT_SOUND = "minecraft:block.note_block.pling";
    private static final String DEFAULT_ICON = "ENDER_EYE";

    public ConfigManager(RecallPotionPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getMessage(String path) {
        return translate(config.getString("messages." + path, "&cMessage not found: " + path));
    }

    public String getAdvancementTitle(String key) {
        return config.getString("achievements." + key + ".title", "Default Title");
    }

    public String getAdvancementDescription(String key) {
        return config.getString("achievements." + key + ".description", "Default Description");
    }

    public Material getAdvancementIcon(String key) {
        String name = config.getString("achievements." + key + ".icon", DEFAULT_ICON).toUpperCase();
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + name + ". Using " + DEFAULT_ICON + ".");
            return Material.valueOf(DEFAULT_ICON);
        }
    }

    public String getAdvancementFrame(String key) {
        String frame = config.getString("achievements." + key + ".frame", "task").toLowerCase();
        if (!frame.equals("task") && !frame.equals("challenge") && !frame.equals("goal")) {
            plugin.getLogger().warning("Invalid advancement frame: " + frame + ". Using 'task'.");
            return "task";
        }
        return frame;
    }

    public String getAdvancementParent(String key) {
        return config.getString("achievements." + key + ".parent", "minecraft:adventure/root");
    }

    public Sound getSound(String path) {
        String raw = config.getString("sounds." + path, DEFAULT_SOUND);
        NamespacedKey key;

        try {
            if (raw.indexOf(':') == -1) {
                String modern = raw.toLowerCase().replace('_', '.');
                key = NamespacedKey.minecraft(modern);
            } else {
                String[] parts = raw.split(":", 2);
                key = new NamespacedKey(parts[0], parts[1]);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Malformed sound key: " + raw + ". Using fallback.");
            key = NamespacedKey.minecraft("block.note_block.pling");
        }

        Sound sound = Registry.SOUNDS.get(key);
        if (sound == null) {
            plugin.getLogger().warning("Unknown sound: " + key + ". Using fallback.");
            sound = Registry.SOUNDS.get(NamespacedKey.minecraft("block.note_block.pling"));
        }
        return sound;
    }

    public int getXpCost() {
        return config.getInt("settings.xp-cost", 8);
    }

    public String getPotionName() {
        return translate(config.getString("settings.potion-name", "&dЗелье возврата"));
    }

    public List<String> getPotionLore() {
        return config.getStringList("settings.potion-lore").stream()
                .map(this::translate)
                .collect(Collectors.toList());
    }

    private String translate(String input) {
        if (input == null) return "";
        var component = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}