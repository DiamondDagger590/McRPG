package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * This attribute allows players to toggle if they want {@link us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest}
 * to pull dropped items towards them.
 */
public class MassHarvestPullItemsAttribute extends OptionalSavingAbilityAttribute<Boolean> implements GuiModifiableAttribute{

    MassHarvestPullItemsAttribute() {
        super("mass_harvest_pull_items_toggled", AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE);
    }

    public MassHarvestPullItemsAttribute(@NotNull Boolean content) {
        super("mass_harvest_pull_items_toggled", AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE, content);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return !getContent();
    }

    @NotNull
    @Override
    public MassHarvestPullItemsAttribute create(@NotNull Boolean content) {
        return new MassHarvestPullItemsAttribute(content);
    }

    @NotNull
    @Override
    public Boolean convertContent(@NotNull String stringContent) {
        return Boolean.parseBoolean(stringContent);
    }

    @NotNull
    @Override
    public Boolean getDefaultContent() {
        return true;
    }

    @NotNull
    public McRPGSlot getSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer1, @NotNull ClickType clickType) {
                SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                skillHolder.getAbilityData(ability).ifPresent(abilityData -> {
                    Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE);
                    if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof MassHarvestPullItemsAttribute massHarvestPullItemsAttribute) {
                        MassHarvestPullItemsAttribute invertedAttribute = new MassHarvestPullItemsAttribute(!massHarvestPullItemsAttribute.getContent());
                        abilityData.addAttribute(invertedAttribute);
                    }
                });
                mcRPGPlayer1.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer1).ifPresent(Gui::refreshGUI);
                return true;
            }

            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                return ItemBuilder.from(mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(getContent() ? LocalizationKey.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_ENABLED_DISPLAY_ITEM : LocalizationKey.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_DISABLED_DISPLAY_ITEM));
            }
        };
    }
}
