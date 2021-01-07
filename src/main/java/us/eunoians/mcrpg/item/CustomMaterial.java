package us.eunoians.mcrpg.item;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.Optional;

/**
 * Custom material enum that holds all the information required to implement custom materials using a resourcepack
 *
 * @author OxKitsune
 */
public enum CustomMaterial {

    DIAMOND_DAGGER(Material.DIAMOND_SWORD, 1);

    /**
     * The base material of the item stack
     */
    private final Material baseMaterial;

    /**
     * The custom model data
     */
    private final int customModelData;

    /**
     * The item stack
     */
    private final ItemStack itemStack;

    CustomMaterial(Material baseMaterial, int customModelData) {
        this.baseMaterial = baseMaterial;
        this.customModelData = customModelData;
        this.itemStack = new ItemStack(baseMaterial);
        this.itemStack.getItemMeta().setCustomModelData(customModelData);

        // Write the custom item type to the persistent data container so we have easy access.
        this.itemStack.getItemMeta().getPersistentDataContainer().set(McRPG.getNamespacedKey("item_type"), PersistentDataType.INTEGER, ordinal());
    }

    /**
     * Get whether the specified {@link ItemStack} is an instance of this {@link CustomMaterial}.
     *
     * @param itemStack the item stack
     *
     * @return {@code true} if the item is an instance of this {@link CustomMaterial} or else {@code false}
     */
    public boolean isMaterial (@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(McRPG.getNamespacedKey("item_type"), PersistentDataType.INTEGER)) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().get(McRPG.getNamespacedKey("item_type"), PersistentDataType.INTEGER).equals(ordinal());
    }

    /**
     * Get the custom material as an {@link ItemStack}.
     *
     * @return the custom item stack
     */
    public ItemStack getItemStack () {
        return itemStack;
    }

    /**
     * Get the custom material as an {@link ItemStack}.
     *
     * @param amount the amount of items in the {@link ItemStack}
     *
     * @return the custom item stack with the specified amount
     */
    public ItemStack getItemStack (int amount) {
        Validate.isTrue(amount > 0 && amount <= 64, "Amount has to be between 1 and 64!");
        ItemStack itemStack = this.itemStack.clone();
        itemStack.setAmount(amount);
        return itemStack;
    }


    /**
     * Get the base material for this {@link CustomMaterial}.
     *
     * @return the base material
     */
    public Material getBaseMaterial() {
        return baseMaterial;
    }

    /**
     * Get the custom model data for this {@link CustomMaterial}.
     *
     * @return the custom model data for this custom material
     */
    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * Attempt to get the {@link CustomMaterial} type from an {@link ItemStack}.
     *
     * @param itemStack the item stack
     *
     * @return the {@link CustomMaterial} type wrapped in an {@link Optional}.
     */
    public static Optional<CustomMaterial> getType (@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return Optional.empty();
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(McRPG.getNamespacedKey("item_type"), PersistentDataType.INTEGER)) return Optional.empty();

        int index = itemStack.getItemMeta().getPersistentDataContainer().get(McRPG.getNamespacedKey("item_type"), PersistentDataType.INTEGER);
        if (values().length < index) return Optional.empty();

        return Optional.of(values()[index]);
    }
}
