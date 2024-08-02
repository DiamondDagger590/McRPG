package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This attribute stores the UUID of an upgrade {@link us.eunoians.mcrpg.quest.Quest} for the ability
 */
public class AbilityUpgradeQuestAttribute extends OptionalSavingAbilityAttribute<UUID> {

    // Use my minecraft UUID for this lol
    private static final UUID DEFAULT_UUID = UUID.fromString("b94b32a4-09e8-4378-905b-0df7805916c1");

    AbilityUpgradeQuestAttribute() {
        super("quest", AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
    }

    public AbilityUpgradeQuestAttribute(@NotNull UUID content) {
        super("quest", AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE, content);
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
