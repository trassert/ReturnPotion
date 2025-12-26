package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class RecipeManager {

    private final RecallPotionPlugin plugin;
    private final NamespacedKey recallPotionKey;
    private static final int CUSTOM_MODEL_DATA = 12345;

    public RecipeManager(RecallPotionPlugin plugin) {
        this.plugin = plugin;
        this.recallPotionKey = new NamespacedKey(plugin, "recall_potion");
    }

    public void registerRecipes() {
        ShapedRecipe recipe = new ShapedRecipe(recallPotionKey, createRecallPotion());
        recipe.shape(" S ", "GWE", "   ");
        recipe.setIngredient('W', Material.POTION);
        recipe.setIngredient('G', Material.SCULK);
        recipe.setIngredient('S', Material.SUNFLOWER);
        recipe.setIngredient('E', Material.ENDER_PEARL);

        plugin.getServer().addRecipe(recipe);
    }

    public void unregisterRecipes() {
        plugin.getServer().removeRecipe(recallPotionKey);
    }

    public ItemStack createRecallPotion() {
        PotionMeta meta = (PotionMeta) new ItemStack(Material.POTION).getItemMeta();
        meta.setBasePotionType(PotionType.WATER);
        meta.setDisplayName(plugin.getConfigManager().getPotionName());
        meta.setLore(plugin.getConfigManager().getPotionLore());
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        ItemStack potion = new ItemStack(Material.POTION);
        potion.setItemMeta(meta);
        return potion;
    }

    public boolean isRecallPotion(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }

        var meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == CUSTOM_MODEL_DATA;
    }
}