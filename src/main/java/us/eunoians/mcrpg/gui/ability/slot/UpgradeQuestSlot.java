package us.eunoians.mcrpg.gui.ability.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Displays the current upgrade quest status for a {@link TierableAbility} inside the Ability Edit GUI.
 * <p>
 * Handles three display states:
 * <ul>
 *   <li>{@link SlotState#ACTIVE_QUEST} — an in-progress upgrade quest; shows overall progress bar,
 *       current objectives, and a click hint to open {@link QuestDetailGui}.</li>
 *   <li>{@link SlotState#LOCKED_BEHIND_LEVEL} — no active quest and the player has not yet reached
 *       the skill level required for the next tier.</li>
 *   <li>{@link SlotState#MAX_TIER_REACHED} — the ability is fully upgraded.</li>
 * </ul>
 */
public class UpgradeQuestSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Ability ability;
    private final SlotState slotState;
    @Nullable
    private final QuestInstance questInstance;

    /**
     * @param mcRPGPlayer   The player viewing the GUI.
     * @param ability       The ability whose upgrade quest state is displayed.
     * @param questInstance The active upgrade quest instance, or {@code null} if none.
     * @param slotState     The resolved display state for this slot.
     */
    public UpgradeQuestSlot(@NotNull McRPGPlayer mcRPGPlayer,
                            @NotNull Ability ability,
                            @Nullable QuestInstance questInstance,
                            @NotNull SlotState slotState) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.ability = ability;
        this.questInstance = questInstance;
        this.slotState = slotState;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return switch (slotState) {
            case ACTIVE_QUEST -> buildActiveQuestItem(mcRPGPlayer);
            case LOCKED_BEHIND_LEVEL -> buildLockedBehindLevelItem(mcRPGPlayer);
            case MAX_TIER_REACHED -> buildMaxTierItem(mcRPGPlayer);
        };
    }

    /**
     * Gets the resolved display state for this slot.
     *
     * @return The current {@link SlotState}.
     */
    @NotNull
    public SlotState getSlotState() {
        return slotState;
    }

    /**
     * Navigates to {@link QuestDetailGui} when the slot is in the {@link SlotState#ACTIVE_QUEST} state.
     * All other states are no-ops that still return {@code true} to consume the click.
     *
     * @param mcRPGPlayer The player who clicked.
     * @param clickType   The click type used.
     * @return {@code true} always.
     */
    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        if (slotState != SlotState.ACTIVE_QUEST || questInstance == null) {
            return true;
        }
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestDetailGui detailGui = QuestDetailGui.forUpgradeQuest(mcRPGPlayer, questInstance, ability);
            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, detailGui);
            player.openInventory(detailGui.getInventory());
        });
        return true;
    }

    /**
     * Builds the item displayed when an active upgrade quest is in progress.
     * Shows the quest name, overall progress bar, current objectives, and a click hint.
     *
     * @param mcRPGPlayer The player for whom the item is being built.
     * @return The configured {@link ItemBuilder}.
     */
    @NotNull
    private ItemBuilder buildActiveQuestItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder itemBuilder = ItemBuilder.from(
                localizationManager.getLocalizedSection(mcRPGPlayer,
                        LocalizationKey.UPGRADE_QUEST_SLOT_ACTIVE_DISPLAY_ITEM));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("ability", ability.getColoredName(mcRPGPlayer));
        placeholders.put("upgrade-quest-progress", questInstance.getOverallProgressBar(20));
        placeholders.put("quest-percent", formatProgressPercent(mcRPGPlayer));
        appendObjectiveSummary(placeholders, localizationManager);

        itemBuilder.addPlaceholders(placeholders);
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }

    /**
     * Builds the item displayed when no upgrade quest is active and the player has not yet
     * reached the skill level required for the next tier.
     *
     * @param mcRPGPlayer The player for whom the item is being built.
     * @return The configured {@link ItemBuilder}.
     */
    @NotNull
    private ItemBuilder buildLockedBehindLevelItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder itemBuilder = ItemBuilder.from(
                localizationManager.getLocalizedSection(mcRPGPlayer,
                        LocalizationKey.UPGRADE_QUEST_SLOT_LOCKED_DISPLAY_ITEM));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("ability", ability.getColoredName(mcRPGPlayer));
        populateTierLevelPlaceholders(placeholders, mcRPGPlayer);

        itemBuilder.addPlaceholders(placeholders);
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }

    /**
     * Builds the item displayed when the ability has reached its maximum tier.
     *
     * @param mcRPGPlayer The player for whom the item is being built.
     * @return The configured {@link ItemBuilder}.
     */
    @NotNull
    private ItemBuilder buildMaxTierItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder itemBuilder = ItemBuilder.from(
                localizationManager.getLocalizedSection(mcRPGPlayer,
                        LocalizationKey.UPGRADE_QUEST_SLOT_MAX_TIER_DISPLAY_ITEM));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("ability", ability.getColoredName(mcRPGPlayer));

        itemBuilder.addPlaceholders(placeholders);
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }

    /**
     * Populates the {@code objective-summary} placeholder by iterating all active quest stages and
     * their objectives, producing one line per objective in the format
     * {@code "• description: current/required"}. Covers all active stages to handle parallel phases.
     * If no active stages exist (edge case — quest completed between GUI open and item build),
     * the placeholder resolves to an empty string.
     *
     * @param placeholders        The placeholder map to populate.
     * @param localizationManager The localization manager used to resolve palette colors in the
     *                            dynamically-built summary string.
     */
    private void appendObjectiveSummary(@NotNull Map<String, String> placeholders,
                                        @NotNull McRPGLocalizationManager localizationManager) {
        if (questInstance == null) {
            placeholders.put("objective-summary", "");
            return;
        }

        List<QuestStageInstance> activeStages = questInstance.getActiveQuestStages();
        if (activeStages.isEmpty()) {
            placeholders.put("objective-summary", "");
            return;
        }

        QuestDefinition def = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION)
                .get(questInstance.getQuestKey())
                .orElse(null);

        Map<NamespacedKey, QuestObjectiveDefinition> objectiveDefMap = buildObjectiveDefMap(def);

        StringBuilder summary = new StringBuilder();
        for (QuestStageInstance stage : activeStages) {
            for (QuestObjectiveInstance obj : stage.getQuestObjectives()) {
                QuestObjectiveDefinition objDef = objectiveDefMap.get(obj.getQuestObjectiveKey());
                String description = objDef != null
                        ? objDef.getDescription(mcRPGPlayer, questInstance.getQuestKey())
                        : obj.getQuestObjectiveKey().getKey();
                if (!summary.isEmpty()) {
                    summary.append("\n");
                }
                summary.append("<body>• ").append(description)
                        .append(": <primary>").append(obj.getCurrentProgression())
                        .append("<body>/<primary>").append(obj.getRequiredProgression());
            }
        }

        placeholders.put("objective-summary", localizationManager.resolvePaletteColors(summary.toString()));
    }

    /**
     * Builds a flat lookup map from objective key to {@link QuestObjectiveDefinition} by walking
     * all phases and stages of the given quest definition. Returns an empty map if the definition
     * is {@code null}.
     *
     * @param def The quest definition to extract objective definitions from, or {@code null}.
     * @return A map of objective key to definition.
     */
    @NotNull
    private Map<NamespacedKey, QuestObjectiveDefinition> buildObjectiveDefMap(@Nullable QuestDefinition def) {
        Map<NamespacedKey, QuestObjectiveDefinition> defMap = new HashMap<>();
        if (def == null) {
            return defMap;
        }
        for (QuestPhaseDefinition phase : def.getPhases()) {
            for (QuestStageDefinition stage : phase.getStages()) {
                for (QuestObjectiveDefinition obj : stage.getObjectives()) {
                    defMap.put(obj.getObjectiveKey(), obj);
                }
            }
        }
        return defMap;
    }

    /**
     * Populates the {@code next-tier-level} and {@code skill} placeholders for the locked state.
     * Reads the current tier from the player's ability data, computes the unlock level for the next
     * tier, and resolves the associated skill name when the ability implements {@link SkillAbility}.
     *
     * @param placeholders The placeholder map to populate.
     * @param player       The player viewing the GUI.
     */
    private void populateTierLevelPlaceholders(@NotNull Map<String, String> placeholders,
                                               @NotNull McRPGPlayer player) {
        if (!(ability instanceof TierableAbility tierableAbility)) {
            placeholders.put("next-tier-level", "?");
            placeholders.put("skill", "");
            return;
        }

        int currentTier = player.asSkillHolder().getAbilityData(ability)
                .flatMap(data -> data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY))
                .filter(attr -> attr instanceof AbilityTierAttribute)
                .map(attr -> ((AbilityTierAttribute) attr).getContent())
                .orElse(1);

        int nextTier = currentTier + 1;
        int unlockLevel = tierableAbility.getUnlockLevelForTier(nextTier);
        placeholders.put("next-tier-level", String.valueOf(unlockLevel));

        if (ability instanceof SkillAbility skillAbility) {
            SkillRegistry skillRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL);
            Optional<Skill> skill = Optional.ofNullable(
                    skillRegistry.registered(skillAbility.getSkillKey())
                            ? skillRegistry.getRegisteredSkill(skillAbility.getSkillKey())
                            : null);
            placeholders.put("skill", skill.map(s -> s.getColoredName(player)).orElse(""));
        } else {
            placeholders.put("skill", "");
        }
    }

    /**
     * Formats the quest's overall progress as a locale-aware percentage string using
     * 0 minimum and 1 maximum fraction digits.
     *
     * @param player The player whose locale determines the number format.
     * @return The formatted percentage string, e.g. {@code "64"} or {@code "64.5"}.
     */
    @NotNull
    private String formatProgressPercent(@NotNull McRPGPlayer player) {
        double percent = questInstance.getOverallProgress() * 100.0;
        McRPGLocalizationManager localizationManager = player.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return localizationManager.getDisplayDecimalFormatter().formatDisplayDecimal(player, percent, 0, 1);
    }

    /**
     * The three possible display states for an {@link UpgradeQuestSlot}.
     */
    public enum SlotState {
        /** The player has an active in-progress upgrade quest for this ability. */
        ACTIVE_QUEST,
        /** No active quest; the ability is not yet at max tier but the player hasn't reached the required level. */
        LOCKED_BEHIND_LEVEL,
        /** The ability has reached its maximum tier and cannot be upgraded further. */
        MAX_TIER_REACHED
    }
}
