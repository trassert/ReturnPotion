package com.trassert.recallpotion.managers;

import com.trassert.recallpotion.RecallPotionPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import java.util.stream.Collectors;

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
        Component nameComp = LegacyComponentSerializer.legacyAmpersand()
            .deserialize(plugin.getConfigManager().getPotionName());
        meta.displayName(nameComp);

        List<Component> loreComp = plugin.getConfigManager().getPotionLore().stream()
            .map(s -> LegacyComponentSerializer.legacyAmpersand().deserialize(s))
            .collect(Collectors.toList());
        meta.lore(loreComp);

        unsafeSetCustomModelData(meta, CUSTOM_MODEL_DATA);
        ItemStack potion = new ItemStack(Material.POTION);
        potion.setItemMeta(meta);
        return potion;
    }

    public boolean isRecallPotion(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }

        var meta = item.getItemMeta();
        return meta != null && unsafeHasCustomModelData(meta) && unsafeGetCustomModelData(meta) == CUSTOM_MODEL_DATA;
    }

    @SuppressWarnings("deprecation")
    private void unsafeSetCustomModelData(PotionMeta meta, int value) {
        meta.setCustomModelData(value);
    }

    @SuppressWarnings("deprecation")
    private boolean unsafeHasCustomModelData(org.bukkit.inventory.meta.ItemMeta meta) {
        return meta.hasCustomModelData();
    }

    @SuppressWarnings("deprecation")
    private int unsafeGetCustomModelData(org.bukkit.inventory.meta.ItemMeta meta) {
        return meta.getCustomModelData();
    }
}