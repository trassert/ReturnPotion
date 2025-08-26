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
import org.bukkit.inventory.EquipmentSlot; // Добавлен импорт
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
        
        // Если игрок держит зелье возврата, отменяем обычное взаимодействие, чтобы оно пилось
        if (event.getItem() != null && plugin.getRecipeManager().isRecallPotion(event.getItem())) {
            // Отменяем событие, чтобы зелье не использовалось как обычный предмет
            // и позволяем PlayerItemConsumeEvent обработать его
            return; // Просто выходим, не отменяем, чтобы зелье можно было пить
        }
        
        // Handle end portal interaction for extracting portal liquid
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
            event.getClickedBlock() != null && 
            event.getClickedBlock().getType() == Material.END_PORTAL) {
            
            // Проверяем, что это основная рука, чтобы избежать двойного срабатывания
            if (event.getHand() == EquipmentSlot.HAND) { 
                if (player.getLevel() >= plugin.getConfigManager().getXpCost()) {
                    // Remove XP
                    player.setLevel(player.getLevel() - plugin.getConfigManager().getXpCost());
                    
                    // Give recall potion
                    ItemStack recallPotion = plugin.getRecipeManager().createRecallPotion();
                    player.getInventory().addItem(recallPotion);
                    
                    // Send message and play sound
                    player.sendMessage(plugin.getConfigManager().getMessage("portal-extract-success"));
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSound("portal-extract"), 1.0f, 1.0f);
                    
                    // Grant achievement
                    plugin.getAdvancementManager().grantPortalExtractAdvancement(player); 
                    
                    event.setCancelled(true); // Отменяем, чтобы не было других взаимодействий с порталом
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("not-enough-xp"));
                    event.setCancelled(true); // Отменяем, чтобы не было других взаимодействий с порталом
                }
            }
        }
    }
    
    // Этот метод теперь вызывается из PlayerConsumeListener
    public void performRecallTeleport(Player player) {
        // Check if player has permission
        if (!player.hasPermission("recallpotion.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        // Get player's bed spawn location or world spawn
        Location homeLocation = player.getBedSpawnLocation();
        if (homeLocation == null) {
            homeLocation = player.getWorld().getSpawnLocation();
        }
        
        if (homeLocation == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-home-set"));
            return;
        }
        
        // Store current location for departure particles
        final Location departureLocation = player.getLocation().clone();
        final Location finalHomeLocation = homeLocation; 
        
        // Teleport player
        player.sendMessage(plugin.getConfigManager().getMessage("potion-used"));
        player.playSound(player.getLocation(), plugin.getConfigManager().getSound("potion-use"), 1.0f, 1.0f);
        
        // Create departure particles (scatter)
        createTeleportParticles(player, departureLocation, true);
        
        // Teleport after a short delay for effect
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.teleport(finalHomeLocation);
            
            // Create arrival particles (suck in)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                createTeleportParticles(player, player.getLocation(), false);
            }, 5L); 
            
            plugin.getAdvancementManager().grantHomeReturnAdvancement(player); 
        }, 20L); // 1 second delay
        
        // Зелье удаляется автоматически после потребления, поэтому здесь не нужно удалять
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
                velocity = new Vector(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1).normalize().multiply(speed);
            } else {
                Vector directionToPlayer = player.getEyeLocation().toVector().subtract(particleLoc.toVector()).normalize();
                velocity = directionToPlayer.multiply(speed);
            }
            
            player.spawnParticle(Particle.END_ROD, particleLoc, 0, velocity.getX(), velocity.getY(), velocity.getZ());
        }
    }
}
