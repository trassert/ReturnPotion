package com.trassert.recallpotion.commands;

import com.trassert.recallpotion.RecallPotionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecallPotionCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "recallpotion.admin";

    private final RecallPotionPlugin plugin;

    public RecallPotionCommand(RecallPotionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            msg(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6RecallPotion Plugin v" + plugin.getPluginMeta().getVersion());
            msg(sender, "usage", "{label}", label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "reload" -> {
                plugin.getConfigManager().reloadConfig();
                msg(sender, "config-reloaded");
            }
            default -> sender.sendMessage("§cНеизвестная команда. Используйте: /" + label + " reload");
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                msg(sender, "player-only");
                return;
            }
            player.getInventory().addItem(plugin.getRecipeManager().createRecallPotion());
            msg(player, "potion-obtained");
            msg(player, "give-self");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            msg(sender, "player-not-found", "{player}", args[1]);
            return;
        }

        target.getScheduler().run(plugin, task -> {
            target.getInventory().addItem(plugin.getRecipeManager().createRecallPotion());
            msg(target, "potion-obtained");
            msg(sender, "give-other", "{player}", target.getName());
        }, null);
    }

    private void msg(CommandSender sender, String key, String... replacements) {
        String message = plugin.getConfigManager().getMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Stream.of("give", "reload"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName), args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(Stream<String> source, String prefix) {
        String lower = prefix.toLowerCase();
        return source.filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}