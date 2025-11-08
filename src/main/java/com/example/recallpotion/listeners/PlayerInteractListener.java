package com.example.recallpotion.listeners;

import com.example.recallpotion.RecallPotionPlugin;
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
        Player player = event.getPlayer();

        if (event.getItem() != null && plugin.getRecipeManager().isRecallPotion(event.getItem())) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock() != null &&
                event.getClickedBlock().getType() == Material.END_PORTAL) {

            if (event.getHand() == EquipmentSlot.HAND) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType() != Material.GLASS_BOTTLE) {
                    return;
                }

                if (player.getLevel() >= plugin.getConfigManager().getXpCost()) {

                    player.setLevel(player.getLevel() - plugin.getConfigManager().getXpCost());

                    ItemStack recallPotion = plugin.getRecipeManager().createRecallPotion();
                    player.getInventory().addItem(recallPotion);

                    player.sendMessage(plugin.getConfigManager().getMessage("portal-extract-success"));
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("portal-extract"), 1.0f,
                            1.0f);

                    plugin.getAdvancementManager().grantPortalExtractAdvancement(player);

                    event.setCancelled(true);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("not-enough-xp"));
                    event.setCancelled(true);
                }
            }
        }
    }

    public void performRecallTeleport(Player player) {

        if (!player.hasPermission("recallpotion.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        Location homeLocation = player.getBedSpawnLocation();
        if (homeLocation == null) {
            homeLocation = player.getWorld().getSpawnLocation();
        }

        if (homeLocation == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-home-set"));
            return;
        }

        final Location departureLocation = player.getLocation().clone();
        final Location finalHomeLocation = homeLocation;

        player.sendMessage(plugin.getConfigManager().getMessage("potion-used"));
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("potion-use"), 1.0f, 1.0f);

        createTeleportParticles(player, departureLocation, true);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.teleport(finalHomeLocation);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                createTeleportParticles(player, player.getLocation(), false);
            }, 5L);

            plugin.getAdvancementManager().grantHomeReturnAdvancement(player);
        }, 20L);

    }

    private void createTeleportParticles(Player player, Location location, boolean scatter) {
        int count = 30;
        double speed = 0.1;

        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() * 2 - 1) * 0.5;
            double offsetY = (random.nextDouble() * 2 - 1) * 0.5;
            double offsetZ = (random.nextDouble() * 2 - 1) * 0.5;

            Location particleLoc = location.clone().add(offsetX, offsetY + 1, offsetZ);

            Vector velocity;
            if (scatter) {
                velocity = new Vector(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1,
                        random.nextDouble() * 2 - 1).normalize().multiply(speed);
            } else {
                Vector directionToPlayer = player.getEyeLocation().toVector().subtract(particleLoc.toVector())
                        .normalize();
                velocity = directionToPlayer.multiply(speed);
            }

            player.spawnParticle(Particle.END_ROD, particleLoc, 0, velocity.getX(), velocity.getY(), velocity.getZ());
        }
    }
}
