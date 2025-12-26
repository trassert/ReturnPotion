package com.example.recallpotion.listeners;

import com.example.recallpotion.RecallPotionPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    private final RecallPotionPlugin plugin;

    public CraftingListener(RecallPotionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (!plugin.getRecipeManager().isRecallPotion(result)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(plugin.getConfigManager().getMessage("potion-obtained"));
            player.playSound(
                    player.getLocation(),
                    plugin.getConfigManager().getSound("potion-obtain"),
                    1.0f,
                    1.0f);
        }, 1L);
    }
}