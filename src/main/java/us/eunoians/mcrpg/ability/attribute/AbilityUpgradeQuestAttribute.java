package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.slot.UpgradeQuestSlot;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

/**
 * This attribute stores the UUID of an active upgrade {@link us.eunoians.mcrpg.quest.impl.QuestInstance}
 * for a {@link us.eunoians.mcrpg.ability.impl.type.TierableAbility}. When set to a non-default
 * value, it indicates the player has an in-progress upgrade quest for the associated ability.
 * The quest UUID stored here references a quest instance managed by the
 * {@link us.eunoians.mcrpg.quest.QuestManager}.
 */
public class AbilityUpgradeQuestAttribute extends OptionalSavingAbilityAttribute<UUID>
        implements GuiModifiableAttribute {

    // Use my minecraft UUID for this lol
    private static final UUID DEFAULT_UUID = UUID.fromString("b94b32a4-09e8-4378-905b-0df7805916c1");

    /**
     * Gets the default UUID used to represent a cleared / unset upgrade quest attribute.
     *
     * @return the default (sentinel) UUID
     */
    @NotNull
    public static UUID defaultUUID() {
        return DEFAULT_UUID;
    }

    AbilityUpgradeQuestAttribute() {
        super("quest", AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
    }

    public AbilityUpgradeQuestAttribute(@NotNull UUID content) {
        super("quest", AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE, content);
    }

    @NotNull
    @Override
    public AbilityAttribute<UUID> create(@NotNull UUID content) {
        return new AbilityUpgradeQuestAttribute(content);
    }

    @NotNull
    @Override
    public UUID convertContent(@NotNull String stringContent) {
        return UUID.fromString(stringContent);
    }

    @NotNull
    @Override
    public UUID getDefaultContent() {
        return DEFAULT_UUID;
    }

    @Override
    public boolean shouldContentBeSaved() {
        return !getContent().equals(DEFAULT_UUID);
    }

    @Override
    public int getDisplayPriority() {
        return 20;
    }

    /**
     * Resolves the display state for this ability's upgrade quest and returns the corresponding
     * {@link UpgradeQuestSlot}. The state resolution order is:
     * <ol>
     *   <li>If the ability is at max tier → {@link UpgradeQuestSlot.SlotState#MAX_TIER_REACHED}.</li>
     *   <li>If a quest UUID is stored and the quest is found as in-progress
     *       → {@link UpgradeQuestSlot.SlotState#ACTIVE_QUEST}.</li>
     *   <li>If the stored UUID is orphaned (no matching quest found), the attribute is reset to its
     *       default value as a self-healing mechanism, and the state falls through to
     *       {@link UpgradeQuestSlot.SlotState#LOCKED_BEHIND_LEVEL}.</li>
     *   <li>Default → {@link UpgradeQuestSlot.SlotState#LOCKED_BEHIND_LEVEL}.</li>
     * </ol>
     *
     * @param mcRPGPlayer The player viewing the GUI.
     * @param ability     The ability whose upgrade quest state is displayed.
     * @return A configured {@link UpgradeQuestSlot}.
     * @throws IllegalArgumentException if {@code ability} is not a {@link TierableAbility}.
     */
    @NotNull
    @Override
    public McRPGSlot getSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        if (!(ability instanceof TierableAbility tierableAbility)) {
            throw new IllegalArgumentException(
                    "UpgradeQuestSlot requires a TierableAbility but received: " + ability.getName(mcRPGPlayer));
        }

        Optional<AbilityData> abilityDataOpt = mcRPGPlayer.asSkillHolder().getAbilityData(ability);
        if (abilityDataOpt.isEmpty()) {
            return buildSlotForState(mcRPGPlayer, ability, null, UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL);
        }

        AbilityData abilityData = abilityDataOpt.get();
        int currentTier = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY)
                .filter(attr -> attr instanceof AbilityTierAttribute)
                .map(attr -> ((AbilityTierAttribute) attr).getContent())
                .orElse(1);

        if (currentTier >= tierableAbility.getMaxTier()) {
            return buildSlotForState(mcRPGPlayer, ability, null, UpgradeQuestSlot.SlotState.MAX_TIER_REACHED);
        }

        QuestInstance activeQuest = resolveActiveUpgradeQuest(mcRPGPlayer, abilityData);
        if (activeQuest != null) {
            return buildSlotForState(mcRPGPlayer, ability, activeQuest, UpgradeQuestSlot.SlotState.ACTIVE_QUEST);
        }

        return buildSlotForState(mcRPGPlayer, ability, null, UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL);
    }

    /**
     * Resolves the active quest instance referenced by this attribute's stored UUID. If the UUID
     * references a quest that no longer exists (orphaned — e.g., due to a server crash or edge-case
     * timing during quest completion), the attribute is reset to its default value as a passive
     * self-healing mechanism.
     *
     * @param mcRPGPlayer The player whose quests to search.
     * @param abilityData The ability data to clear the attribute on if orphaned.
     * @return The active in-progress quest instance, or {@code null} if none exists.
     */
    @Nullable
    private QuestInstance resolveActiveUpgradeQuest(@NotNull McRPGPlayer mcRPGPlayer,
                                                    @NotNull AbilityData abilityData) {
        if (!shouldContentBeSaved()) {
            return null;
        }
        QuestManager questManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        QuestInstance quest = questManager.getActiveQuestsForPlayer(mcRPGPlayer.getUUID()).stream()
                .filter(q -> q.getQuestUUID().equals(getContent()))
                .findFirst()
                .orElse(null);
        if (quest == null) {
            abilityData.addAttribute(new AbilityUpgradeQuestAttribute());
        }
        return quest;
    }

    /**
     * Constructs an {@link UpgradeQuestSlot} for the given state.
     *
     * @param mcRPGPlayer   The player viewing the GUI.
     * @param ability       The ability whose upgrade quest state is displayed.
     * @param questInstance The active quest instance, or {@code null} when not applicable.
     * @param state         The resolved slot state.
     * @return A configured {@link UpgradeQuestSlot}.
     */
    @NotNull
    private McRPGSlot buildSlotForState(@NotNull McRPGPlayer mcRPGPlayer,
                                        @NotNull Ability ability,
                                        @Nullable QuestInstance questInstance,
                                        @NotNull UpgradeQuestSlot.SlotState state) {
        return new UpgradeQuestSlot(mcRPGPlayer, ability, questInstance, state);
    }
}
