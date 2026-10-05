package com.trassert.recallpotion.listeners;

import com.trassert.recallpotion.RecallPotionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
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

        var player = event.getPlayer();

        if (!playerInteractListener.canRecall(player)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        var originalItem = event.getItem();
        EquipmentSlot hand = event.getHand();
        int slot = (hand == EquipmentSlot.HAND)
                ? player.getInventory().getHeldItemSlot()
                : 40; // offhand slot

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