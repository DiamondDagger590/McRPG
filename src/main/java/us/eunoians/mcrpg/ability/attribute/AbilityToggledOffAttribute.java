package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

/**
 * This attribute stores if an ability is toggled off or not. So a value of
 * {@code true} would mean the ability is toggled off.
 */
public class AbilityToggledOffAttribute extends AbilityAttribute<Boolean> {

    AbilityToggledOffAttribute() {
        super("toggled", AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
    }

    public AbilityToggledOffAttribute(@NotNull Boolean content) {
        super("tier", AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY, content);
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
}
