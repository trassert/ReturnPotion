package com.example.recallpotion.listeners;

import com.example.recallpotion.RecallPotionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumeListener implements Listener {

    private final RecallPotionPlugin plugin;
    private final PlayerInteractListener playerInteractListener; // Ссылка на PlayerInteractListener

    public PlayerConsumeListener(RecallPotionPlugin plugin) {
        this.plugin = plugin;
        // Получаем существующий экземпляр PlayerInteractListener
        // Это не идеальный способ, но для простоты примера подойдет.
        // В более сложных плагинах лучше использовать Dependency Injection.
        this.playerInteractListener = new PlayerInteractListener(plugin); 
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (plugin.getRecipeManager().isRecallPotion(event.getItem())) {
            event.setCancelled(true); // Отменяем стандартное потребление, чтобы не было эффектов обычной бутылки
            playerInteractListener.performRecallTeleport(event.getPlayer()); // Вызываем логику телепортации
            
            // Удаляем зелье вручную, так как мы отменили событие потребления
            // Если не отменять событие, то зелье удалится само, но могут быть нежелательные эффекты
            if (event.getItem().getAmount() > 1) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            } else {
                event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), null);
            }
        }
    }
}
