package com.trassert.recallpotion.commands;

import com.trassert.recallpotion.RecallPotionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecallPotionCommand implements CommandExecutor, TabCompleter {

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

        String version = plugin.getPluginMeta().getVersion();

        if (args.length == 0) {
            sender.sendMessage("§6RecallPotion Plugin v" + version);
            String usage = plugin.getConfigManager().getMessage("usage").replace("{label}", label);
            sender.sendMessage(usage);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
                    return true;
                }
                Player player = (Player) sender;
                ItemStack potion = plugin.getRecipeManager().createRecallPotion();
                player.getInventory().addItem(potion);
                player.sendMessage(plugin.getConfigManager().getMessage("potion-obtained"));
                player.sendMessage(plugin.getConfigManager().getMessage("give-self"));
                return true;
            } else {
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found").replace("{player}", args[1]));
                    return true;
                }
                ItemStack potion = plugin.getRecipeManager().createRecallPotion();
                target.getInventory().addItem(potion);
                target.sendMessage(plugin.getConfigManager().getMessage("potion-obtained"));
                sender.sendMessage(plugin.getConfigManager().getMessage("give-other").replace("{player}", target.getName()));
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
            return true;
        }

        sender.sendMessage("§cНеизвестная команда. Используйте: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("give");
            subs.add("reload");
            String pref = args[0].toLowerCase();
            return subs.stream().filter(s -> s.startsWith(pref)).collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String pref = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(pref))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}