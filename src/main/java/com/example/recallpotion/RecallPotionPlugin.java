package com.example.recallpotion;

import com.example.recallpotion.commands.RecallPotionCommand;
import com.example.recallpotion.listeners.PlayerInteractListener;
import com.example.recallpotion.listeners.CraftingListener;
import com.example.recallpotion.listeners.PlayerConsumeListener; // Добавлен новый слушатель
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
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.recipeManager = new RecipeManager(this);
        this.advancementManager = new AdvancementManager(this); 
        
        // Load configuration
        configManager.loadConfig();
        
        // Register recipes
        recipeManager.registerRecipes();

        // Register advancements
        advancementManager.registerAdvancements();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConsumeListener(this), this); // Регистрация нового слушателя
        
        // Register commands
        getCommand("recallpotion").setExecutor(new RecallPotionCommand(this));
        
        getLogger().info("RecallPotion plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Remove custom recipes
        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
        }

        // Unregister advancements
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
