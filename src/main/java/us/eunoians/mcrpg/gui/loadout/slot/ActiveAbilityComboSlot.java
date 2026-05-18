package us.eunoians.mcrpg.gui.loadout.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.combo.ComboPattern;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutAbilitySelectGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Set;

/**
 * Represents one of the three fixed active-ability combo slots in the {@link LoadoutGui} combo zone.
 * <p>
 * Each slot is permanently bound to a {@link ComboPattern}. When occupied, the ability's normal
 * display item is shown with the combo pattern and interaction hints appended to its lore. When
 * empty, a light-blue placeholder item shows the same pattern and an assignment prompt.
 * <p>
 * Left-clicking opens a filtered {@link LoadoutAbilitySelectGui} in
 * {@link LoadoutAbilitySelectGui.SelectionMode#ACTIVE} mode, showing only
 * {@link us.eunoians.mcrpg.ability.combo.ComboActivatable} abilities.
 * <p>
 * Number key presses 1–3 on this slot reorder active abilities between combo positions.
 * Because the hotbar-button index is only available on the raw {@link org.bukkit.event.inventory.InventoryClickEvent},
 * {@link ClickType#NUMBER_KEY} is consumed silently here; {@link LoadoutGui} handles the
 * actual reorder logic via its own {@code InventoryClickEvent} handler.
 */
public class ActiveAbilityComboSlot implements McRPGSlot {

    private final Loadout loadout;
    private final ComboPattern comboPattern;
    @Nullable
    private final Ability ability;

    /**
     * Constructs an empty combo slot — no ability is assigned to this combo position yet.
     *
     * @param loadout      The loadout this slot belongs to.
     * @param comboPattern The combo pattern permanently assigned to this slot position.
     */
    public ActiveAbilityComboSlot(@NotNull Loadout loadout, @NotNull ComboPattern comboPattern) {
        this.loadout = loadout;
        this.comboPattern = comboPattern;
        this.ability = null;
    }

    /**
     * Constructs an occupied combo slot for the given ability.
     *
     * @param loadout      The loadout this slot belongs to.
     * @param comboPattern The combo pattern permanently assigned to this slot position.
     * @param ability      The active ability currently occupying this combo slot.
     */
    public ActiveAbilityComboSlot(@NotNull Loadout loadout, @NotNull ComboPattern comboPattern, @NotNull Ability ability) {
        this.loadout = loadout;
        this.comboPattern = comboPattern;
        this.ability = ability;
    }

    /**
     * Returns the {@link ComboPattern} for this slot.
     *
     * @return The {@link ComboPattern} for this slot.
     */
    @NotNull
    public ComboPattern getComboPattern() {
        return comboPattern;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        if (clickType == ClickType.NUMBER_KEY) {
            // Reorder is handled at the LoadoutGui level because the hotbar button index
            // is only available on the raw InventoryClickEvent.
            return true;
        }
        openAbilitySelectGui(mcRPGPlayer);
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        if (ability != null) {
            return buildOccupiedItem(mcRPGPlayer);
        }
        return buildEmptyItem(mcRPGPlayer);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }

    /**
     * Opens a {@link LoadoutAbilitySelectGui} filtered to {@link LoadoutAbilitySelectGui.SelectionMode#ACTIVE}
     * so only {@link us.eunoians.mcrpg.ability.combo.ComboActivatable} abilities are shown.
     *
     * @param mcRPGPlayer The player clicking the slot.
     */
    private void openAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            LoadoutAbilitySelectGui gui;
            if (ability != null) {
                gui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout, ability.getAbilityKey(), LoadoutAbilitySelectGui.SelectionMode.ACTIVE);
            } else {
                gui = new LoadoutAbilitySelectGui(mcRPGPlayer, loadout, LoadoutAbilitySelectGui.SelectionMode.ACTIVE);
            }
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, gui);
            player.openInventory(gui.getInventory());
        });
    }

    /**
     * Builds the display item for an occupied combo slot.
     * <p>
     * Appends combo pattern information and interaction hints to the ability's standard lore.
     * If the ability is a {@link TierableAbility} and has an active upgrade quest, a progress
     * summary is appended after the standard hints so players can check upgrade status at a glance.
     * <p>
     * Uses {@code addDisplayLoreComponent} rather than {@code addDisplayLore} because
     * {@link us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder} stores the original
     * ability lore in the Component list, which overwrites the String list at build time.
     *
     * @param mcRPGPlayer The player viewing this slot.
     * @return The {@link ItemBuilder} for the occupied slot.
     */
    @NotNull
    private ItemBuilder buildOccupiedItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        String patternDisplay = localizationManager.getLocalizedMessage(mcRPGPlayer, comboPattern.getLocalizationKey());
        List<String> additionalLore = localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_OCCUPIED_ADDITIONAL_LORE);
        var builder = ability.getDisplayItemBuilder(mcRPGPlayer);
        builder.addPlaceholder("combo-pattern", patternDisplay);
        var miniMessage = McRPG.getInstance().getMiniMessage();
        for (String line : additionalLore) {
            String resolved = line.replace("<combo-pattern>", patternDisplay);
            builder.addDisplayLoreComponent(miniMessage.deserialize(resolved).decoration(TextDecoration.ITALIC, false));
        }
        appendUpgradeQuestProgress(mcRPGPlayer, builder);
        return builder;
    }

    /**
     * Conditionally appends upgrade quest progress lore to {@code builder} when the ability
     * has an active upgrade quest. Does nothing if the ability is not a {@link TierableAbility},
     * if it is not unlocked, or if no upgrade quest is currently in progress.
     *
     * @param mcRPGPlayer The player viewing this slot.
     * @param builder     The item builder to append lore to.
     */
    private void appendUpgradeQuestProgress(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ItemBuilder builder) {
        if (!(ability instanceof TierableAbility)) {
            return;
        }
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(ability);
        if (abilityDataOptional.isEmpty()) {
            return;
        }
        var abilityData = abilityDataOptional.get();
        boolean isUnlocked = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE)
                .map(value -> value instanceof AbilityUnlockedAttribute attribute && attribute.getContent())
                .orElse(true);
        if (!isUnlocked) {
            return;
        }
        var questAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
        if (questAttributeOptional.isEmpty() || !(questAttributeOptional.get() instanceof AbilityUpgradeQuestAttribute questAttribute)
                || !questAttribute.shouldContentBeSaved()) {
            return;
        }
        QuestManager questManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        QuestInstance activeQuest = questManager.getActiveQuestsForPlayer(mcRPGPlayer.getUUID()).stream()
                .filter(q -> q.getQuestUUID().equals(questAttribute.getContent()))
                .findFirst()
                .orElse(null);
        if (activeQuest == null) {
            return;
        }
        String progressBar = activeQuest.getOverallProgressBar(20);
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        List<String> progressLore = localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_UPGRADE_QUEST_PROGRESS_LORE);
        var miniMessage = McRPG.getInstance().getMiniMessage();
        for (String line : progressLore) {
            String resolved = line.replace("<upgrade-quest-progress>", progressBar);
            builder.addDisplayLoreComponent(miniMessage.deserialize(resolved).decoration(TextDecoration.ITALIC, false));
        }
    }

    /**
     * Builds the placeholder display item for an empty combo slot.
     *
     * @param mcRPGPlayer The player viewing this slot.
     * @return The {@link ItemBuilder} for the empty slot.
     */
    @NotNull
    private ItemBuilder buildEmptyItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        String patternDisplay = localizationManager.getLocalizedMessage(mcRPGPlayer, comboPattern.getLocalizationKey());
        ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_EMPTY_DISPLAY_ITEM))
                .addPlaceholder("combo-pattern", patternDisplay);
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }
}
