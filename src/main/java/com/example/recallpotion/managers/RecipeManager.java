package com.example.recallpotion.managers;

import com.example.recallpotion.RecallPotionPlugin;
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

        ItemStack recallPotion = createRecallPotion();

        ShapedRecipe recipe = new ShapedRecipe(recallPotionKey, recallPotion);
        recipe.shape(" S ", "GWE", "   ");
        recipe.setIngredient('W', Material.POTION);
        recipe.setIngredient('G', Material.SCULK);
        recipe.setIngredient('S', Material.SUNFLOWER);
        recipe.setIngredient('E', Material.ENDER_EYE);

        plugin.getServer().addRecipe(recipe);
    }

    public void unregisterRecipes() {
        plugin.getServer().removeRecipe(recallPotionKey);
    }

    public ItemStack createRecallPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.setBasePotionType(PotionType.WATER);

        meta.setDisplayName(plugin.getConfigManager().getPotionName());
        meta.setLore(plugin.getConfigManager().getPotionLore());

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
