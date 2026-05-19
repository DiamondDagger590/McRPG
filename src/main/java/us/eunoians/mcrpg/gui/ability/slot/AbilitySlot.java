package us.eunoians.mcrpg.gui.ability.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityToggledOffAttribute;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.ManaAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityLoreAppender;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.gui.ability.AbilityAttributeEditGui;
import us.eunoians.mcrpg.gui.ability.AbilityGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used in {@link us.eunoians.mcrpg.gui.ability.AbilityGui}s to represent an {@link Ability}
 * while providing click actions for said ability.
 */
public class AbilitySlot implements McRPGSlot {

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
                    AbilityAttributeEditGui abilityAttributeEditGui = new AbilityAttributeEditGui(mcRPGPlayer, ability);
                    player.closeInventory();
                    guiManager.trackPlayerGui(mcRPGPlayer.getUUID(), abilityAttributeEditGui);
                    player.openInventory(abilityAttributeEditGui.getInventory());
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
        McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        var miniMessage = McRPG.getInstance().getMiniMessage();

        ItemBuilder itemBuilder = ability.getDisplayItemBuilder(mcRPGPlayer);

        appendTypeLore(itemBuilder, mcRPGPlayer, localizationManager, miniMessage);
        appendManaCostLore(itemBuilder, mcRPGPlayer, localizationManager, miniMessage);
        appendStatusLore(itemBuilder, mcRPGPlayer, localizationManager, miniMessage);
        // Blank separator between the combined stats block and click hints
        itemBuilder.addDisplayLoreComponent(Component.empty());
        appendClickHints(itemBuilder, mcRPGPlayer, localizationManager, miniMessage);

        // AbilityLoreAppender content (quest progress, upgrade locked, ability locked, expansion pack)
        var loreAppender = AbilityLoreAppender.getAppendLore(mcRPGPlayer, ability);
        Map<String, String> placeholders = loreAppender.getRight();
        itemBuilder.addPlaceholders(placeholders);
        for (String line : loreAppender.getLeft()) {
            String resolved = line;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                resolved = resolved.replace("<" + entry.getKey() + ">", entry.getValue());
            }
            resolved = localizationManager.resolvePaletteColors(resolved);
            itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(resolved)
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Enchantment glint: on when enabled (toggle = false), off when disabled (toggle = true)
        mcRPGPlayer.asSkillHolder().getAbilityData(ability)
                .flatMap(data -> data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY))
                .ifPresent(attr -> itemBuilder.setEnchantGlint(
                        !((AbilityToggledOffAttribute) attr).getContent()));

        return itemBuilder;
    }

    /**
     * Resolves the locale {@link Route} for the ability's type tag lore line.
     * <p>
     * Classification rules (in priority order):
     * <ol>
     *   <li>{@link ComboActivatable} → Active</li>
     *   <li>{@link PassiveAbility} with {@link AbilityAttributeRegistry#ABILITY_UNLOCKED_ATTRIBUTE} in applicable attributes → Passive</li>
     *   <li>All others → Innate (always present in loadouts, no unlock requirement)</li>
     * </ol>
     *
     * @param ability The ability to classify.
     * @return The locale route for the type tag line.
     */
    @NotNull
    private Route resolveTypeRoute(@NotNull Ability ability) {
        if (ability instanceof ComboActivatable) {
            return LocalizationKey.ABILITY_LORE_TYPE_ACTIVE;
        }
        if (ability instanceof PassiveAbility
                && ability.getApplicableAttributes().contains(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE)) {
            return LocalizationKey.ABILITY_LORE_TYPE_PASSIVE;
        }
        return LocalizationKey.ABILITY_LORE_TYPE_INNATE;
    }

    /**
     * Appends the ability type tag lore line (e.g., "Type: Active").
     *
     * @param itemBuilder         The builder to append to.
     * @param mcRPGPlayer         The player context.
     * @param localizationManager The localization manager.
     * @param miniMessage         The MiniMessage instance.
     */
    private void appendTypeLore(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer,
                                @NotNull McRPGLocalizationManager localizationManager,
                                @NotNull MiniMessage miniMessage) {
        Route typeRoute = resolveTypeRoute(ability);
        String typeLine = localizationManager.getLocalizedMessage(mcRPGPlayer, typeRoute);
        itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(typeLine)
                .decoration(TextDecoration.ITALIC, false));
    }

    /**
     * Appends the mana cost lore line if the ability is a {@link ManaAbility} with a positive cost.
     * Uses the player's ability holder to resolve the tier-dependent mana cost.
     * Formats via {@link us.eunoians.mcrpg.localization.McRPGDisplayDecimalFormatter} with 0 min / 2 max
     * fraction digits so integers display cleanly (e.g., "30") while fractional costs show up to 2 decimals.
     *
     * @param itemBuilder         The builder to append to.
     * @param mcRPGPlayer         The player context.
     * @param localizationManager The localization manager.
     * @param miniMessage         The MiniMessage instance.
     */
    private void appendManaCostLore(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer,
                                    @NotNull McRPGLocalizationManager localizationManager,
                                    @NotNull MiniMessage miniMessage) {
        if (!(ability instanceof ManaAbility manaAbility)) {
            return;
        }
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        int manaCost = manaAbility.getManaCost(skillHolder);
        if (manaCost <= 0) {
            return;
        }
        String formattedCost = localizationManager.getDisplayDecimalFormatter()
                .formatDisplayDecimal(mcRPGPlayer, manaCost, 0, 2);
        String manaLine = localizationManager.getLocalizedMessage(mcRPGPlayer,
                LocalizationKey.ABILITY_LORE_MANA_COST_LINE);
        manaLine = manaLine.replace("<mana-cost>", formattedCost);
        itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(manaLine)
                .decoration(TextDecoration.ITALIC, false));
    }

    /**
     * Appends the toggle status lore line ("Status: Enabled" or "Status: Disabled") if the
     * ability has an {@link AbilityToggledOffAttribute} in its {@link AbilityData}.
     *
     * @param itemBuilder         The builder to append to.
     * @param mcRPGPlayer         The player context.
     * @param localizationManager The localization manager.
     * @param miniMessage         The MiniMessage instance.
     */
    private void appendStatusLore(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer,
                                  @NotNull McRPGLocalizationManager localizationManager,
                                  @NotNull MiniMessage miniMessage) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);
        if (abilityDataOptional.isEmpty()) {
            return;
        }
        Optional<AbilityAttribute<?>> toggleAttr = abilityDataOptional.get()
                .getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
        if (toggleAttr.isEmpty() || !(toggleAttr.get() instanceof AbilityToggledOffAttribute toggled)) {
            return;
        }
        boolean isToggledOff = toggled.getContent();
        Route statusRoute = isToggledOff
                ? LocalizationKey.ABILITY_LORE_STATUS_DISABLED
                : LocalizationKey.ABILITY_LORE_STATUS_ENABLED;
        String statusLine = localizationManager.getLocalizedMessage(mcRPGPlayer, statusRoute);
        itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(statusLine)
                .decoration(TextDecoration.ITALIC, false));
    }

    /**
     * Appends click hint lore lines. Toggle hint is shown only if the ability has an
     * {@link AbilityToggledOffAttribute}; configure hint is always shown.
     * Bedrock players (Geyser) receive a single "Click to configure" hint since they
     * cannot distinguish left-click from right-click.
     *
     * @param itemBuilder         The builder to append to.
     * @param mcRPGPlayer         The player context.
     * @param localizationManager The localization manager.
     * @param miniMessage         The MiniMessage instance.
     */
    private void appendClickHints(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer,
                                  @NotNull McRPGLocalizationManager localizationManager,
                                  @NotNull MiniMessage miniMessage) {
        var geyserOptional = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.GEYSER);
        boolean isBedrock = geyserOptional.isPresent()
                && geyserOptional.get().isBedrockPlayer(mcRPGPlayer.getUUID());

        if (isBedrock) {
            String bedrockHint = localizationManager.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.ABILITY_LORE_HINT_CONFIGURE_BEDROCK);
            itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(bedrockHint)
                    .decoration(TextDecoration.ITALIC, false));
            return;
        }

        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);

        if (abilityDataOptional.isPresent()) {
            Optional<AbilityAttribute<?>> toggleAttr = abilityDataOptional.get()
                    .getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
            if (toggleAttr.isPresent() && toggleAttr.get() instanceof AbilityToggledOffAttribute toggled) {
                Route toggleRoute = toggled.getContent()
                        ? LocalizationKey.ABILITY_LORE_HINT_TOGGLE_ENABLE
                        : LocalizationKey.ABILITY_LORE_HINT_TOGGLE_DISABLE;
                String toggleLine = localizationManager.getLocalizedMessage(mcRPGPlayer, toggleRoute);
                itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(toggleLine)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        String configLine = localizationManager.getLocalizedMessage(mcRPGPlayer,
                LocalizationKey.ABILITY_LORE_HINT_CONFIGURE);
        itemBuilder.addDisplayLoreComponent(miniMessage.deserialize(configLine)
                .decoration(TextDecoration.ITALIC, false));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(AbilityGui.class);
    }
}
