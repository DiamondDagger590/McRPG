package us.eunoians.mcrpg.loadout;

import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;

import java.util.Optional;

/**
 * A loadout display is the visual component to a {@link Loadout}, allowing users
 * to customize their individual loadouts.
 */
public final class LoadoutDisplay implements Cloneable {

    private CustomItemWrapper displayItem;
    @Nullable
    private String displayName;

    public LoadoutDisplay(@NotNull ItemStack itemStack, @NotNull String displayName) {
        this.displayItem = new CustomItemWrapper(itemStack);
        this.displayName = displayName;
    }

    public LoadoutDisplay(@NotNull Material material, @Nullable String displayName) {
        this.displayItem = new CustomItemWrapper(material);
        this.displayName = displayName;
    }

    public LoadoutDisplay(@NotNull CustomItemWrapper displayItem, @Nullable String displayName) {
        this.displayItem = displayItem;
        this.displayName = displayName;
    }

    /**
     * Sets the {@link Material} and custom model data from the provided {@link ItemStack} to be used
     * for this display.
     *
     * @param itemStack The {@link ItemStack} to use as the display item.
     */
    public void setDisplayItem(@NotNull ItemStack itemStack) {
        displayItem = new CustomItemWrapper(itemStack);
    }

    /**
     * Sets the {@link Material} to be used in this display.
     *
     * @param material The {@link Material} to be used in the display.
     */
    public void setDisplayItem(@NotNull Material material) {
        this.displayItem = new CustomItemWrapper(material);
    }

    /**
     * Sets the custom model to be used in this display.
     *
     * @param customModel The custom model to use in this display
     */
    public void setDisplayItem(@NotNull String customModel) {
        this.displayItem = new CustomItemWrapper(customModel);
    }

    /**
     * Sets the display name for this display.
     *
     * @param displayName The {@link String} to use as a display name.
     */
    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    /**
     * The {@link Material} used by this display.
     *
     * @return The {@link Material} used by this display.
     */
    @NotNull
    public CustomItemWrapper getDisplayItem() {
        return displayItem;
    }

    /**
     * Gets the {@link String} used as the display name for the display.
     *
     * @return An {@link Optional} containing the {@link Optional} used as the display name for the display, or an empty
     * one if the default display name should be used.
     */
    @NotNull
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoadoutDisplay loadoutDisplay) {
            return getDisplayItem().equals(loadoutDisplay.getDisplayItem())
                    && getDisplayName().equals(loadoutDisplay.getDisplayName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return displayItem.hashCode() * (displayName != null ? displayName.hashCode() : 1);
    }

    @NotNull
    @Override
    protected Object clone() {
        return new LoadoutDisplay(displayItem, displayName);
    }

    @NotNull
    private McRPG getMcRPG() {
        return McRPG.getInstance();
    }
}
