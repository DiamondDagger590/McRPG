package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

/**
 * This attribute stores the cooldown that an ability has left before it can be activated again
 */
public class AbilityCooldownAttribute extends OptionalAbilityAttribute<Long> {

    AbilityCooldownAttribute() {
        super("cooldown", AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
    }

    public AbilityCooldownAttribute(@NotNull Long content) {
        super("cooldown", AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY, content);
    }

    /**
     * Creates a new instance of this {@link AbilityCooldownAttribute} class, containing the provided {@link Long} content as the value
     *
     * @param cooldown The {@link Long} content to be used as the value in the returned {@link AbilityCooldownAttribute}
     * @return A new instance of this {@link AbilityCooldownAttribute} class, containing the provided {@link Long} content as the value
     */
    @NotNull
    @Override
    public AbilityCooldownAttribute create(@NotNull Long cooldown) {
        return new AbilityCooldownAttribute(cooldown);
    }

    /**
     * Converts the provided {@link String} content into content that matches the type of {@link Long}.
     * <p>
     * This serves to allow abstraction to exist and all values to be stored as strings inside of {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param stringContent The {@link String} content to be converted into type {@link Long}
     * @return The {@link String} content that is now converted into {@link Long} content
     */
    @NotNull
    @Override
    public Long convertContent(@NotNull String stringContent) {
        return Long.parseLong(stringContent);
    }

    /**
     * Gets the default content value for this attribute. This should be considered the "default state" for this attribute, such
     * as a tier defaulting to 0.
     * <p>
     * The largest use case for this is populating {@link AbilityAttributeManager} with initial instances of this class, which can then
     * be built on using {@link #create(Long)}.
     *
     * @return {@code 0} as a {@link Long}.
     */
    @NotNull
    @Override
    public Long getDefaultContent() {
        return 0L;
    }

    @Override
    public boolean shouldContentBeSaved(@NotNull Long content) {
        return content <= 0 || content < System.currentTimeMillis();
    }
}
