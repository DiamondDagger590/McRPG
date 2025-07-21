package us.eunoians.mcrpg.gui.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.builder.item.ability.AbilityLoreAppender;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.gui.ability.AbilityEditGui;
import us.eunoians.mcrpg.gui.ability.AbilityGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Optional;
import java.util.Set;

/**
 * This slot is used in {@link us.eunoians.mcrpg.gui.ability.AbilityGui}s to represent an {@link Ability}
 * while providing click actions for said ability.
 */
public class AbilitySlot extends McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Ability ability;

    public AbilitySlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.ability = ability;
    }

    /**
     * Gets the {@link McRPGPlayer} creating this slot.
     *
     * @return The {@link McRPGPlayer} creating this slot.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    /**
     * Gets the {@link Ability} represented by this slot.
     *
     * @return The {@link Ability} represented by this slot.
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        McRPGGuiManager guiManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI);
        var guiOptional = guiManager.getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
            playerOptional.ifPresent(player -> {
                // If the player is using geyser, we have custom logic for them since they don't have right/left clicks. (Or if they just did a left click lol)
                var geyserOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.GEYSER);
                if ((geyserOptional.isPresent() && geyserOptional.get().isBedrockPlayer(mcRPGPlayer.getUUID())) || clickType == ClickType.RIGHT) {
                    AbilityEditGui abilityEditGui = new AbilityEditGui(mcRPGPlayer, ability);
                    player.closeInventory();
                    guiManager.trackPlayerGui(mcRPGPlayer.getUUID(), abilityEditGui);
                    player.openInventory(abilityEditGui.getInventory());
                }
                // If they're on java and right-clicked
                else if (clickType == ClickType.LEFT) {
                    SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
                    skillHolder.getAbilityData(ability).ifPresent(abilityData -> {
                        Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
                        if (abilityAttributeOptional.isPresent() && abilityAttributeOptional.get() instanceof AbilityToggledOffAttribute toggledOffAttribute) {
                            AbilityToggledOffAttribute abilityToggledOffAttribute = new AbilityToggledOffAttribute(!toggledOffAttribute.getContent());
                            abilityData.addAttribute(abilityToggledOffAttribute);
                            gui.refreshGUI();
                        }
                    });
                }
            });
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var itemBuilder = ability.getDisplayItemBuilder(mcRPGPlayer);
        var loreAppender = AbilityLoreAppender.getAppendLore(mcRPGPlayer, ability);
        itemBuilder.addDisplayLore(loreAppender.getLeft());
        itemBuilder.addPlaceholders(loreAppender.getRight());
        return itemBuilder;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(AbilityGui.class);
    }
}
