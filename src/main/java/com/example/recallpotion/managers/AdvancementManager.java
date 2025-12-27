package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class AdvancementManager {

    private final RecallPotionPlugin plugin;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final NamespacedKey homeReturnKey;
    private final NamespacedKey portalExtractKey;

    public AdvancementManager(RecallPotionPlugin plugin) {
        this.plugin = plugin;
        this.homeReturnKey = new NamespacedKey(plugin, "home_return");
        this.portalExtractKey = new NamespacedKey(plugin, "portal_extract");
    }

    public void registerAdvancements() {
        registerAdvancement(homeReturnKey, "home-return");
        registerAdvancement(portalExtractKey, "portal-extract");
    }

    private void registerAdvancement(NamespacedKey key, String configPath) {
        var cfg = plugin.getConfigManager();
        String json = createAdvancementJson(
                cfg.getAdvancementTitle(configPath),
                cfg.getAdvancementDescription(configPath),
                cfg.getAdvancementIcon(configPath),
                cfg.getAdvancementFrame(configPath),
                cfg.getAdvancementParent(configPath));

        try {
            Bukkit.getUnsafe().loadAdvancement(key, json);
            plugin.getLogger().info("Registered advancement: " + key.getKey());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Advancement '" + key.getKey() + "' may already exist: " + e.getMessage());
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
        JsonObject root = new JsonObject();

        JsonObject display = new JsonObject();
        display.add("title", createTextComponent(title));
        display.add("description", createTextComponent(description));

        JsonObject iconObj = new JsonObject();
        iconObj.addProperty("item", "minecraft:" + icon.name().toLowerCase());
        display.add("icon", iconObj);

        display.addProperty("frame", frame);
        display.addProperty("show_toast", true);
        display.addProperty("announce_to_chat", true);
        display.addProperty("hidden", false);
        root.add("display", display);

        root.addProperty("parent", parent);

        JsonObject criteria = new JsonObject();
        JsonObject trigger = new JsonObject();
        trigger.addProperty("trigger", "minecraft:impossible");
        criteria.add("impossible", trigger);
        root.add("criteria", criteria);

        return GSON.toJson(root);
    }

    private JsonObject createTextComponent(String text) {
        JsonObject obj = new JsonObject();
        obj.addProperty("text", text);
        return obj;
    }

    public void grantHomeReturnAdvancement(Player player) {
        grantAdvancement(player, homeReturnKey);
    }

    public void grantPortalExtractAdvancement(Player player) {
        grantAdvancement(player, portalExtractKey);
    }

    private void grantAdvancement(Player player, NamespacedKey key) {
        Advancement adv = Bukkit.getAdvancement(key);
        if (adv == null) {
            plugin.getLogger().warning("Advancement not found: " + key);
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(adv);
        if (!progress.isDone()) {
            progress.getRemainingCriteria().forEach(progress::awardCriteria);
        }
    }

    private void revokeAdvancement(Player player, NamespacedKey key) {
        Advancement adv = Bukkit.getAdvancement(key);
        if (adv == null)
            return;

        AdvancementProgress progress = player.getAdvancementProgress(adv);
        if (progress.isDone()) {
            progress.getAwardedCriteria().forEach(progress::revokeCriteria);
        }
    }
}