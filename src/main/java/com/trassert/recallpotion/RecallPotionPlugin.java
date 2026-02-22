package com.trassert.recallpotion;

import com.trassert.recallpotion.listeners.*;
import com.trassert.recallpotion.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class RecallPotionPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private RecipeManager recipeManager;
    private AdvancementManager advancementManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        recipeManager = new RecipeManager(this);
        advancementManager = new AdvancementManager(this);

        configManager.loadConfig();
        recipeManager.registerRecipes();
        advancementManager.registerAdvancements();

        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new CraftingListener(this), this);
        pm.registerEvents(new PlayerConsumeListener(this), this);

        var recallCmd = getCommand("recallpotion");
        var recallExecutor = new com.trassert.recallpotion.commands.RecallPotionCommand(this);
        recallCmd.setExecutor(recallExecutor);
        recallCmd.setTabCompleter((org.bukkit.command.TabCompleter) recallExecutor);

        getLogger().info("RecallPotion plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        recipeManager.unregisterRecipes();
        advancementManager.unregisterAdvancements();

        getLogger().info("RecallPotion plugin has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public AdvancementManager getAdvancementManager() {
        return advancementManager;
    }
}