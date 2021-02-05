package us.eunoians.mcrpg.ability.configurable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.ConfigurableAbility;
import us.eunoians.mcrpg.api.error.AbilityDisplayItemNotFoundException;

import java.util.Objects;

/**
 * This interface represents an {@link us.eunoians.mcrpg.ability.Ability} that should have it's display item pulled
 * from a config file.
 *
 * @author DiamondDagger590
 */
public interface ConfigurableAbilityDisplayItem extends ConfigurableAbility {

    /**
     * Gets the {@link ConfigurationSection} containing display item information for the ability
     *
     * @return The {@link ConfigurationSection} that contains display item information for the ability
     * @throws AbilityDisplayItemNotFoundException if there is a null {@link ConfigurationSection} returned
     */
    @NotNull
    public default ConfigurationSection getDisplayItemSection() throws AbilityDisplayItemNotFoundException {

        ConfigurationSection configurationSection = getAbilityConfigurationFile().getConfigurationSection("display-item");

        if (configurationSection == null) {
            throw new AbilityDisplayItemNotFoundException("Configuration section known as: 'display-item' is missing from the " + this.getAbilityConfigurationFile().getName() + " file.", getAbilityID());
        }
        return configurationSection;
    }

    /**
     * Gets the {@link ItemStack} to represent this ability in GUI's
     *
     * @return The {@link ItemStack} to represent this ability in GUI's
     */
    @Override
    @NotNull
    public default ItemStack getDisplayItem() throws AbilityDisplayItemNotFoundException{

        ConfigurationSection displayItemSection = getDisplayItemSection();

        Material material = Material.getMaterial(Objects.requireNonNull(displayItemSection.getString("material", "STONE")));
        int amount = displayItemSection.getInt("amount", 1);
        String displayName = displayItemSection.getString("display-name");

        //TODO finish
        return null;
    }
}
