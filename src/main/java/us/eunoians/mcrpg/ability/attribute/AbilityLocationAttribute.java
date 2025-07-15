package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.Location;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;

/**
 * This attribute stores a {@link Location} value for a given ability
 */
public class AbilityLocationAttribute extends OptionalSavingAbilityAttribute<Location> implements GuiModifiableAttribute {

    private static final Location DEFAULT_LOCATION = new Location(null, 0, 0, 0);

    AbilityLocationAttribute() {
        super("location", AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE);
    }

    public AbilityLocationAttribute(@NotNull Location content) {
        super("location", AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE, content);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent().getWorld() != null;
    }

    @NotNull
    @Override
    public AbilityAttribute<Location> create(@NotNull Location content) {
        return new AbilityLocationAttribute(content);
    }

    @NotNull
    @Override
    public Location convertContent(@NotNull String stringContent) {
        Optional<Location> optionalLocation = Methods.deserializeLocation(stringContent);
        if (optionalLocation.isEmpty()) {
            throw new RuntimeException("Expected a location to be able to be deserialized, but it wasn't. Serialized content was :" + stringContent);
        }
        return optionalLocation.get();
    }

    @NotNull
    @Override
    public Location getDefaultContent() {
        return DEFAULT_LOCATION;
    }

    @NotNull
    @Override
    public String serializeContent() {
        return Methods.serializeLocation(getContent());
    }

    @NotNull
    @Override
    public McRPGSlot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability) {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                return false;
            }

            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                if (shouldContentBeSaved()) {
                    ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.LOCATION_ATTRIBUTE_LOCATION_SAVED_DISPLAY_ITEM));
                    itemBuilder.setPlaceholders(Map.of(
                            "location-x", Double.toString(getContent().getBlockX()),
                            "location-y", Double.toString(getContent().getBlockY()),
                            "location-z", Double.toString(getContent().getBlockZ()),
                            "location-world", getContent().getWorld() == null ? "null" : getContent().getWorld().getName(),
                            "location", getContent().toBlockLocation().toString()));
                    return itemBuilder;
                }
                else {
                    return ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.LOCATION_ATTRIBUTE_NO_LOCATION_SAVED_DISPLAY_ITEM));
                }
            }
        };
    }
}
