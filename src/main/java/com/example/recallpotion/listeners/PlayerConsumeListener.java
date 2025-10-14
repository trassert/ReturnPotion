package com.example.recallpotion.listeners;

import com.example.recallpotion.RecallPotionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumeListener implements Listener {

    private final RecallPotionPlugin plugin;
    private final PlayerInteractListener playerInteractListener;

    public PlayerConsumeListener(RecallPotionPlugin plugin) {
        this.plugin = plugin;

        this.playerInteractListener = new PlayerInteractListener(plugin);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (plugin.getRecipeManager().isRecallPotion(event.getItem())) {
            event.setCancelled(true);
            playerInteractListener.performRecallTeleport(event.getPlayer());

            if (event.getItem().getAmount() > 1) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            } else {
                event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), null);
            }
        }
    }
}
