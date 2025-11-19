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

            final var player = event.getPlayer();
            final var item = event.getItem().clone();

            player.getScheduler().run(plugin, scheduledTask -> {

                playerInteractListener.performRecallTeleport(player);

                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item);
                } else {
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
                }
            }, null);
        }
    }
}