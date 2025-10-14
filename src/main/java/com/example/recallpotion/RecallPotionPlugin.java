package com.example.recallpotion;

import com.example.recallpotion.commands.RecallPotionCommand;
import com.example.recallpotion.listeners.PlayerInteractListener;
import com.example.recallpotion.listeners.CraftingListener;
import com.example.recallpotion.listeners.PlayerConsumeListener;
import com.example.recallpotion.managers.ConfigManager;
import com.example.recallpotion.managers.RecipeManager;
import com.example.recallpotion.managers.AdvancementManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RecallPotionPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private RecipeManager recipeManager;
    private AdvancementManager advancementManager;

    @Override
    public void onEnable() {

        this.configManager = new ConfigManager(this);
        this.recipeManager = new RecipeManager(this);
        this.advancementManager = new AdvancementManager(this);

        configManager.loadConfig();

        recipeManager.registerRecipes();

        advancementManager.registerAdvancements();

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConsumeListener(this), this);

        getCommand("recallpotion").setExecutor(new RecallPotionCommand(this));

        getLogger().info("RecallPotion plugin has been enabled!");
    }

    @Override
    public void onDisable() {

        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
        }

        if (advancementManager != null) {
            advancementManager.unregisterAdvancements();
        }

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
