package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin; // Убедитесь, что здесь правильный импорт
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class RecipeManager {
    
    private final RecallPotionPlugin plugin;
    private NamespacedKey recallPotionKey;
    
    public RecipeManager(RecallPotionPlugin plugin) {
        this.plugin = plugin;
        this.recallPotionKey = new NamespacedKey(plugin, "recall_potion");
    }
    
    public void registerRecipes() {
        // Create recall potion item
        ItemStack recallPotion = createRecallPotion();
        
        // Create shaped recipe
        ShapedRecipe recipe = new ShapedRecipe(recallPotionKey, recallPotion);
        recipe.shape(" S ", "GWE", "   ");
        recipe.setIngredient('W', Material.POTION); // Water bottle
        recipe.setIngredient('G', Material.SCULK); // Sculk block
        recipe.setIngredient('S', Material.SUNFLOWER);
        recipe.setIngredient('E', Material.ENDER_EYE);
        
        // Register recipe
        plugin.getServer().addRecipe(recipe);
    }
    
    public void unregisterRecipes() {
        plugin.getServer().removeRecipe(recallPotionKey);
    }
    
    public ItemStack createRecallPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        
        // Используем новый API вместо устаревшего PotionData
        meta.setBasePotionType(PotionType.WATER);
        
        // Set custom name and lore
        meta.setDisplayName(plugin.getConfigManager().getPotionName());
        meta.setLore(plugin.getConfigManager().getPotionLore());
        
        // Add custom model data to distinguish from regular potions
        meta.setCustomModelData(12345);
        
        potion.setItemMeta(meta);
        return potion;
    }
    
    public boolean isRecallPotion(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 12345;
    }
}
