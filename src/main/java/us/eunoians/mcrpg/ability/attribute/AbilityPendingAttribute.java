package us.eunoians.mcrpg.ability.attribute;

import org.jetbrains.annotations.NotNull;

/**
 * This attribute stores the pending status for an ability, being {@code true} if the ability
 * is pending being accepted by the player.
 */
public class AbilityPendingAttribute extends AbilityAttribute<Boolean> {

    AbilityPendingAttribute() {
        super("pending_status", AbilityAttributeManager.ABILITY_PENDING_ATTRIBUTE_KEY);
    }

    public AbilityPendingAttribute(@NotNull Boolean abilityPending) {
        super("pending_status", AbilityAttributeManager.ABILITY_PENDING_ATTRIBUTE_KEY, abilityPending);
    }

    /**
     * Creates a new instance of this {@link AbilityPendingAttribute} class, containing the provided {@link Boolean} content as the value
     *
     * @param abilityPending The {@link Boolean} content to be used as the value in the returned {@link AbilityPendingAttribute}
     * @return A new instance of this {@link AbilityPendingAttribute} class, containing the provided {@link Boolean} content as the value
     */
    @NotNull
    @Override
    public AbilityPendingAttribute create(@NotNull Boolean abilityPending) {
        return new AbilityPendingAttribute(abilityPending);
    }

    /**
     * Converts the provided {@link String} content into content that matches the type of {@link Boolean}.
     * <p>
     * This serves to allow abstraction to exist and all values to be stored as strings inside of {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param stringContent The {@link String} content to be converted into type {@link Boolean}
     * @return The {@link String} content that is now converted into {@link Boolean} content
     */
    @NotNull
    @Override
    public Boolean convertContent(@NotNull String stringContent) {
        return stringContent.equals("1") || Boolean.parseBoolean(stringContent);
    }

    /**
     * Gets the default content value for this attribute. This should be considered the "default state" for this attribute, such
     * as a tier defaulting to 0.
     * <p>
     * The largest use case for this is populating {@link AbilityAttributeManager} with initial instances of this class, which can then
     * be built on using {@link #create(Boolean)}.
     *
     * @return {@code false} as the default value.
     */
    @NotNull
    @Override
    public Boolean getDefaultContent() {
        return false;
    }
}
