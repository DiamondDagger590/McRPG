package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * This attribute stores if an ability is toggled off or not. So a value of
 * {@code true} would mean the ability is toggled off.
 */
public class AbilityToggledOffAttribute extends OptionalSavingAbilityAttribute<Boolean> implements GuiModifiableAttribute {

    AbilityToggledOffAttribute() {
        super("toggled", AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
    }

    public AbilityToggledOffAttribute(@NotNull Boolean content) {
        super("toggled", AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY, content);
    }

    @NotNull
    @Override
    public AbilityAttribute<Boolean> create(@NotNull Boolean content) {
        return new AbilityToggledOffAttribute(content);
    }

    @NotNull
    @Override
    public Boolean convertContent(@NotNull String stringContent) {
        return Boolean.parseBoolean(stringContent);
    }

    @NotNull
    @Override
    public Boolean getDefaultContent() {
        return false;
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent();
    }

    @Override
    @NotNull
    public McRPGSlot getSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer1, @NotNull ClickType clickType) {
                SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                skillHolder.getAbilityData(ability).ifPresent(abilityData -> {
                    Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
                    if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                        AbilityToggledOffAttribute abilityToggledOffAttribute = new AbilityToggledOffAttribute(!toggledOffAttribute.getContent());
                        abilityData.addAttribute(abilityToggledOffAttribute);
                    }
                });
                mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer1).ifPresent(Gui::refreshGUI);
                return true;
            }

            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                return ItemBuilder.from(mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(getContent() ? LocalizationKey.ABILITY_TOGGLED_OFF_ATTRIBUTE_DISPLAY_ITEM : LocalizationKey.ABILITY_TOGGLED_ON_ATTRIBUTE_DISPLAY_ITEM));
            }
        };
    }
}
