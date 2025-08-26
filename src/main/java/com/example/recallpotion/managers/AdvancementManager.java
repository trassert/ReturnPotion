package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement; // Добавлен импорт
import com.google.gson.JsonParser; // Добавлен импорт
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.logging.Level;

public class AdvancementManager { 
    
    private final RecallPotionPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private NamespacedKey homeReturnKey;
    private NamespacedKey portalExtractKey;
    
    public AdvancementManager(RecallPotionPlugin plugin) {
        this.plugin = plugin;
        this.homeReturnKey = new NamespacedKey(plugin, "home_return");
        this.portalExtractKey = new NamespacedKey(plugin, "portal_extract");
    }
    
    public void registerAdvancements() {
        // Home Return Advancement
        String homeReturnJson = createAdvancementJson(
            plugin.getConfigManager().getAdvancementTitle("home-return"), 
            plugin.getConfigManager().getAdvancementDescription("home-return"), 
            plugin.getConfigManager().getAdvancementIcon("home-return"),
            plugin.getConfigManager().getAdvancementFrame("home-return"),
            plugin.getConfigManager().getAdvancementParent("home-return")
        );
        try {
            Bukkit.getUnsafe().loadAdvancement(homeReturnKey, homeReturnJson);
            plugin.getLogger().info("Registered advancement: " + homeReturnKey.getKey());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load home_return advancement (already exists?): " + e.getMessage());
        }

        // Portal Extract Advancement
        String portalExtractJson = createAdvancementJson(
            plugin.getConfigManager().getAdvancementTitle("portal-extract"), 
            plugin.getConfigManager().getAdvancementDescription("portal-extract"), 
            plugin.getConfigManager().getAdvancementIcon("portal-extract"),
            plugin.getConfigManager().getAdvancementFrame("portal-extract"),
            plugin.getConfigManager().getAdvancementParent("portal-extract")
        );
        try {
            Bukkit.getUnsafe().loadAdvancement(portalExtractKey, portalExtractJson);
            plugin.getLogger().info("Registered advancement: " + portalExtractKey.getKey());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load portal_extract advancement (already exists?): " + e.getMessage());
        }
    }

    public void unregisterAdvancements() {
        // Remove advancements from all players first
        for (Player player : Bukkit.getOnlinePlayers()) {
            revokeAdvancement(player, homeReturnKey);
            revokeAdvancement(player, portalExtractKey);
        }

        // Then remove from server
        Bukkit.getUnsafe().removeAdvancement(homeReturnKey);
        Bukkit.getUnsafe().removeAdvancement(portalExtractKey);
        plugin.getLogger().info("Unregistered custom advancements.");
    }

    private String createAdvancementJson(String title, String description, Material icon, String frame, String parent) {
        return gson.toJson(new AdvancementJson(title, description, icon.name().toLowerCase(), frame, parent));
    }

    // Helper class for JSON serialization
    private static class AdvancementJson {
        Display display;
        String parent;
        Criteria criteria;

        AdvancementJson(String title, String description, String iconMaterial, String frame, String parent) {
            this.display = new Display(title, description, iconMaterial, frame);
            this.parent = parent;
            this.criteria = new Criteria();
        }
    }

    private static class Display {
        JsonElement title; // Изменено на JsonElement
        JsonElement description; // Изменено на JsonElement
        Icon icon;
        String frame;
        boolean show_toast = true;
        boolean announce_to_chat = true;
        boolean hidden = false;

        Display(String title, String description, String iconMaterial, String frame) {
            this.icon = new Icon(iconMaterial);
            this.title = JsonParser.parseString("{\"text\":\"" + title + "\"}"); // Парсим как JsonElement
            this.description = JsonParser.parseString("{\"text\":\"" + description + "\"}"); // Парсим как JsonElement
            this.frame = frame;
        }
    }

    private static class Icon {
        String id; 
        Icon(String item) {
            this.id = "minecraft:" + item; 
        }
    }

    private static class Criteria {
        Trigger trigger = new Trigger();
    }

    private static class Trigger {
        String trigger = "minecraft:impossible"; 
    }
    
    public void grantHomeReturnAdvancement(Player player) {
        grantAdvancement(player, homeReturnKey);
    }
    
    public void grantPortalExtractAdvancement(Player player) {
        grantAdvancement(player, portalExtractKey);
    }

    private void grantAdvancement(Player player, NamespacedKey key) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone()) {
                for (String criteria : progress.getRemainingCriteria()) {
                    progress.awardCriteria(criteria);
                }
            }
        } else {
            plugin.getLogger().warning("Advancement " + key.getKey() + " not found for player " + player.getName());
        }
    }

    private void revokeAdvancement(Player player, NamespacedKey key) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                for (String criteria : progress.getAwardedCriteria()) {
                    progress.revokeCriteria(criteria);
                }
            }
        }
    }
}
