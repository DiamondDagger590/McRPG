package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

/**
 * This attribute stores if an ability has been unlocked or not
 */
public class AbilityUnlockedAttribute extends OptionalAbilityAttribute<Boolean> {

    AbilityUnlockedAttribute() {
        super("unlocked", AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE);
    }

    public AbilityUnlockedAttribute(@NotNull Boolean content) {
        super("unlocked", AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE, content);
    }

    @NotNull
    @Override
    public AbilityAttribute<Boolean> create(@NotNull Boolean content) {
        return new AbilityUnlockedAttribute(content);
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
    public boolean shouldContentBeSaved(@NotNull Boolean content) {
        return content;
    }
}
