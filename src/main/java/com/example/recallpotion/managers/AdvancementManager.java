package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

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

        String homeReturnJson = createAdvancementJson(
                plugin.getConfigManager().getAdvancementTitle("home-return"),
                plugin.getConfigManager().getAdvancementDescription("home-return"),
                plugin.getConfigManager().getAdvancementIcon("home-return"),
                plugin.getConfigManager().getAdvancementFrame("home-return"),
                plugin.getConfigManager().getAdvancementParent("home-return"));
        try {
            Bukkit.getUnsafe().loadAdvancement(homeReturnKey, homeReturnJson);
            plugin.getLogger().info("Registered advancement: " + homeReturnKey.getKey());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to load home_return advancement (already exists?): " + e.getMessage());
        }

        String portalExtractJson = createAdvancementJson(
                plugin.getConfigManager().getAdvancementTitle("portal-extract"),
                plugin.getConfigManager().getAdvancementDescription("portal-extract"),
                plugin.getConfigManager().getAdvancementIcon("portal-extract"),
                plugin.getConfigManager().getAdvancementFrame("portal-extract"),
                plugin.getConfigManager().getAdvancementParent("portal-extract"));
        try {
            Bukkit.getUnsafe().loadAdvancement(portalExtractKey, portalExtractJson);
            plugin.getLogger().info("Registered advancement: " + portalExtractKey.getKey());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to load portal_extract advancement (already exists?): " + e.getMessage());
        }
    }

    public void unregisterAdvancements() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            revokeAdvancement(player, homeReturnKey);
            revokeAdvancement(player, portalExtractKey);
        }

        Bukkit.getUnsafe().removeAdvancement(homeReturnKey);
        Bukkit.getUnsafe().removeAdvancement(portalExtractKey);
        plugin.getLogger().info("Unregistered custom advancements.");
    }

    private String createAdvancementJson(String title, String description, Material icon, String frame, String parent) {
        return gson.toJson(new AdvancementJson(title, description, icon.name().toLowerCase(), frame, parent));
    }

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
        Display(String title, String description, String iconMaterial, String frame) {
            new Icon(iconMaterial);
            JsonParser.parseString("{\"text\":\"" + title + "\"}");
            JsonParser.parseString("{\"text\":\"" + description + "\"}");
        }
    }

    private static class Icon {
        Icon(String item) {
        }
    }

    private static class Criteria {
    }

    private static class Trigger {
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
