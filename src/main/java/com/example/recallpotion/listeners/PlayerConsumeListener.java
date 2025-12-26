package com.example.recallpotion.listeners;

import com.example.recallpotion.RecallPotionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerConsumeListener implements Listener {

    private final RecallPotionPlugin plugin;
    private final PlayerInteractListener playerInteractListener;

    public PlayerConsumeListener(RecallPotionPlugin plugin) {
        this.plugin = plugin;
        this.playerInteractListener = new PlayerInteractListener(plugin);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!plugin.getRecipeManager().isRecallPotion(event.getItem())) {
            return;
        }

        event.setCancelled(true);

        var player = event.getPlayer();
        var originalItem = event.getItem();
        int slot = player.getInventory().getHeldItemSlot();

        player.getScheduler().run(plugin, task -> {
            playerInteractListener.performRecallTeleport(player);

            if (originalItem.getAmount() > 1) {
                ItemStack updated = originalItem.clone();
                updated.setAmount(updated.getAmount() - 1);
                player.getInventory().setItem(slot, updated);
            } else {
                player.getInventory().setItem(slot, null);
            }
        }, null);
    }
}