package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This attribute stores the UUID of an active upgrade {@link us.eunoians.mcrpg.quest.impl.QuestInstance}
 * for a {@link us.eunoians.mcrpg.ability.impl.type.TierableAbility}. When set to a non-default
 * value, it indicates the player has an in-progress upgrade quest for the associated ability.
 * The quest UUID stored here references a quest instance managed by the
 * {@link us.eunoians.mcrpg.quest.QuestManager}.
 */
public class AbilityUpgradeQuestAttribute extends OptionalSavingAbilityAttribute<UUID> {

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
}
