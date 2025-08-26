package com.example.recallpotion.commands;

import com.example.recallpotion.RecallPotionPlugin; // Убедитесь, что здесь правильный импорт
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecallPotionCommand implements CommandExecutor {
    
    private final RecallPotionPlugin plugin;
    
    public RecallPotionCommand(RecallPotionPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("recallpotion.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§6RecallPotion Plugin v1.0.0");
            sender.sendMessage("§7Использование: /recallpotion reload");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
            return true;
        }
        
        sender.sendMessage("§cНеизвестная команда. Используйте: /recallpotion reload");
        return true;
    }
}
