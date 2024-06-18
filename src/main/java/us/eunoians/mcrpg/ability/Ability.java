package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponentAttribute;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponent;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponentAttribute;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.exception.ability.EventNotRegisteredForActivation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An ability doesn't always belong to a skill, while a skill will always have abilities
 * tied to it.
 */
public abstract class Ability {

    private final NamespacedKey abilityKey;
    private final List<EventCancellingComponentAttribute> cancellingComponents;
    private final Map<Class<? extends Event>, List<EventActivatableComponentAttribute>> activatingAttributes;

    public Ability(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
        this.cancellingComponents = new ArrayList<>();
        this.activatingAttributes = new HashMap<>();
    }

    /**
     * Gets the {@link NamespacedKey} of this ability.
     *
     * @return The {@link NamespacedKey} of this ability.
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    /**
     * Gets a {@link Set} of all {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute AbilityAttributes} that
     * this ability utilizes.
     *
     * @return A {@link Set} of all {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute AbilityAttributes} that
     * this ability utilizes.
     */
    public Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
    }

    /**
     * Checks to see if this ability belongs to a {@link us.eunoians.mcrpg.skill.Skill}
     *
     * @return {@code true} if the ability belongs to a {@link us.eunoians.mcrpg.skill.Skill}
     */
    public boolean belongsToSkill() {
        return getSkill().isPresent();
    }

    /**
     * Gets an {@link Optional} that will be empty or contain the {@link NamespacedKey} of the
     * {@link us.eunoians.mcrpg.skill.Skill} this ability belongs to.
     *
     * @return An {@link Optional} that will be empty or contain the {@link NamespacedKey} of the
     * {@link us.eunoians.mcrpg.skill.Skill} this ability belongs to.
     */
    public abstract Optional<NamespacedKey> getSkill();

    /**
     * Gets an {@link Optional} that will be empty or contain the legacy name of this ability.
     * <p>
     * This is only used for abilities that existed before the recode in order to support
     * legacy database table conversions.
     *
     * @return An {@link Optional} that will be empty or contain the legacy name of this ability.
     */
    public Optional<String> getLegacyName() {
        return Optional.empty();
    }

    /**
     * Gets an {@link Optional} containing the database name for an ability. This is an internal
     * use only name that is used for database storage.
     * <p>
     * The {@link Optional} will be empty if this is a legacy ability since there is code to convert
     * {@link #getLegacyName()} to its old form for this use.
     *
     * @return An {@link Optional} containing the database name for an ability. This is an internal
     * use only name that is used for database storage.
     */
    public abstract Optional<String> getDatabaseName();

    public abstract String getDisplayName();

    public abstract ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder);

    public abstract void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event);

    // TODO finish
    public Optional<EventCancellingComponent> checkIfComponentCancels(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        for (EventCancellingComponentAttribute eventCancellingComponentAttribute : cancellingComponents) {
            EventCancellingComponent eventCancellingComponent = eventCancellingComponentAttribute.abilityComponent();
        }
        return null;
    }

    public boolean canEventActivateAbility(@NotNull Event event) {
        return activatingAttributes.containsKey(event.getClass());
    }

    @NotNull
    public Optional<EventActivatableComponent> checkIfComponentFailsActivation(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        if (!canEventActivateAbility(event)) {
            throw new EventNotRegisteredForActivation(event, this);
        }

        EventActivatableComponent returnComponent = null;
        for (EventActivatableComponentAttribute eventActivatableComponentAttribute : getActivatingComponents(event.getClass())) {
            EventActivatableComponent eventActivatableComponent = eventActivatableComponentAttribute.abilityComponent();

            if (!eventActivatableComponent.shouldActivate(abilityHolder, event)) {
                returnComponent = eventActivatableComponent;
                break;
            }
        }

        return Optional.ofNullable(returnComponent);
    }

    public void addCancellingComponent(@NotNull EventCancellingComponent eventCancellingComponent, @NotNull Class<? extends Event> clazz,
                                       int priority) {
        cancellingComponents.add(new EventCancellingComponentAttribute(eventCancellingComponent, clazz, priority));
        sortCancellingComponents();
    }

    public void addActivatableComponent(@NotNull EventActivatableComponent eventActivatableComponent, @NotNull Class<? extends Event> clazz,
                                        int priority) {
        List<EventActivatableComponentAttribute> newAttributes = getActivatingComponents(clazz);
        newAttributes.add(new EventActivatableComponentAttribute(eventActivatableComponent, clazz, priority));
        activatingAttributes.put(clazz, newAttributes);
        sortActivatingComponents();
    }

    private List<EventActivatableComponentAttribute> getActivatingComponents(Class<? extends Event> clazz) {
        return activatingAttributes.getOrDefault(clazz, new ArrayList<>());
    }

    private void sortCancellingComponents() {
        cancellingComponents.sort(Comparator.comparingInt(EventCancellingComponentAttribute::priority));
    }

    private void sortActivatingComponents() {
        for (Class<? extends Event> clazz : activatingAttributes.keySet()) {
            List<EventActivatableComponentAttribute> attributes = getActivatingComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventActivatableComponentAttribute::priority));
        }
    }
}
