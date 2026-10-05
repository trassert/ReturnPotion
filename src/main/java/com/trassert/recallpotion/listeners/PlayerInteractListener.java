package com.trassert.recallpotion.listeners;

import com.trassert.recallpotion.RecallPotionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class PlayerInteractListener implements Listener {

    private final RecallPotionPlugin plugin;
    private final Random random = new Random();

    public PlayerInteractListener(RecallPotionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && plugin.getRecipeManager().isRecallPotion(item)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.END_PORTAL
                || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() != Material.GLASS_BOTTLE) {
            return;
        }

        int xpCost = plugin.getConfigManager().getXpCost();
        if (player.getLevel() < xpCost) {
            msg(player, "not-enough-xp");
            event.setCancelled(true);
            return;
        }

        player.setLevel(player.getLevel() - xpCost);

        ItemStack recallPotion = plugin.getRecipeManager().createRecallPotion();
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(itemInHand);
            var leftover = player.getInventory().addItem(recallPotion);
            leftover.values().forEach(drop ->
                    player.getWorld().dropItemNaturally(player.getLocation(), drop));
        } else {
            player.getInventory().setItemInMainHand(recallPotion);
        }

        msg(player, "portal-extract-success");
        playSound(player, "portal-extract");
        plugin.getAdvancementManager().grantPortalExtractAdvancement(player);

        event.setCancelled(true);
    }

    public boolean canRecall(Player player) {
        if (!player.hasPermission("recallpotion.use")) {
            msg(player, "no-permission");
            return false;
        }
        if (player.getRespawnLocation() == null) {
            msg(player, "no-home-set");
            return false;
        }
        return true;
    }

    public void performRecallTeleport(Player player) {
        Location home = player.getRespawnLocation();
        if (home == null) {
            msg(player, "no-home-set");
            return;
        }

        final Location finalHome = home;
        Location departure = player.getLocation().clone();

        msg(player, "potion-used");
        playSound(player, "potion-use");
        createTeleportParticles(player, departure, true);

        player.getScheduler().runDelayed(plugin, task -> {
            player.teleportAsync(finalHome).thenAccept(success -> {
                if (Boolean.TRUE.equals(success)) {
                    player.getScheduler().runDelayed(plugin, task2 -> {
                        createTeleportParticles(player, player.getLocation(), false);
                        plugin.getAdvancementManager().grantHomeReturnAdvancement(player);
                    }, null, 5L);
                }
            });
        }, null, 20L);
    }

    private void createTeleportParticles(Player player, Location location, boolean scatter) {
        final int count = 30;
        final double speed = 0.1;

        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() * 2 - 1) * 0.5;
            double offsetY = (random.nextDouble() * 2 - 1) * 0.5;
            double offsetZ = (random.nextDouble() * 2 - 1) * 0.5;

            Location particleLoc = location.clone().add(offsetX, offsetY + 1, offsetZ);

            Vector velocity;
            if (scatter) {
                velocity = new Vector(
                        random.nextDouble() * 2 - 1,
                        random.nextDouble() * 2 - 1,
                        random.nextDouble() * 2 - 1).normalize().multiply(speed);
            } else {
                Vector direction = player.getEyeLocation().toVector()
                        .subtract(particleLoc.toVector())
                        .normalize();
                velocity = direction.multiply(speed);
            }

            player.spawnParticle(Particle.END_ROD, particleLoc, 0,
                    velocity.getX(), velocity.getY(), velocity.getZ());
        }
    }

    private void msg(Player player, String key) {
        player.sendMessage(plugin.getConfigManager().getMessage(key));
    }

    private void playSound(Player player, String key) {
        player.playSound(player.getLocation(),
                plugin.getConfigManager().getSound(key), 1.0f, 1.0f);
    }
}