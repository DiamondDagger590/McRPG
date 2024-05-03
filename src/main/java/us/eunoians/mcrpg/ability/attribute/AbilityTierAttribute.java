package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

/**
 * This attribute stores the tier for an ability.
 */
public class AbilityTierAttribute extends OptionalAbilityAttribute<Integer> implements DisplayableAttribute {

    AbilityTierAttribute() {
        super("tier", AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
    }

    public AbilityTierAttribute(@NotNull Integer content) {
        super("tier", AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY, content);
    }

    /**
     * Creates a new instance of this {@link AbilityTierAttribute} class, containing the provided {@link Integer} content as the value
     *
     * @param tier The {@link Integer} content to be used as the value in the returned {@link AbilityTierAttribute}
     * @return A new instance of this {@link AbilityTierAttribute} class, containing the provided {@link Integer} content as the value
     */
    @NotNull
    @Override
    public AbilityTierAttribute create(@NotNull Integer tier) {
        return new AbilityTierAttribute(tier);
    }

    /**
     * Converts the provided {@link String} content into content that matches the type of {@link Integer}.
     * <p>
     * This serves to allow abstraction to exist and all values to be stored as strings inside of {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param stringContent The {@link String} content to be converted into type {@link Integer}
     * @return The {@link String} content that is now converted into {@link Integer} content
     */
    @NotNull
    @Override
    public Integer convertContent(@NotNull String stringContent) {
        return Integer.parseInt(stringContent);
    }

    /**
     * Gets the default content value for this attribute. This should be considered the "default state" for this attribute, such
     * as a tier defaulting to 0.
     * <p>
     * The largest use case for this is populating {@link AbilityAttributeManager} with initial instances of this class, which can then
     * be built on using {@link #create(Integer)}.
     *
     * @return {@code 0} as an {@link Integer}.
     */
    @NotNull
    @Override
    public Integer getDefaultContent() {
        return 1;
    }

    @Override
    public boolean shouldContentBeSaved() {
        return getContent() > 1;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Tier";
    }
}
