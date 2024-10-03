package us.eunoians.mcrpg.ability.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponentAttribute;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponent;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponentAttribute;
import us.eunoians.mcrpg.ability.component.readyable.EventReadyableComponent;
import us.eunoians.mcrpg.ability.component.readyable.EventReadyableComponentAttribute;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.exception.ability.EventNotRegisteredForActivationException;
import us.eunoians.mcrpg.exception.ability.EventNotRegisteredForReadyingException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
public abstract class BaseAbility implements Ability {

    protected static final NumberFormat FORMAT = new DecimalFormat("#0.00");

    private final NamespacedKey abilityKey;
    private final List<EventCancellingComponentAttribute> cancellingComponents;
    private final Map<Class<? extends Event>, List<EventActivatableComponentAttribute>> activatingAttributes;
    private final Map<Class<? extends Event>, List<EventReadyableComponentAttribute>> readyAttributes;
    private final Plugin plugin;

    public BaseAbility(@NotNull Plugin plugin, @NotNull NamespacedKey abilityKey) {
        this.plugin = plugin;
        this.abilityKey = abilityKey;
        this.cancellingComponents = new ArrayList<>();
        this.activatingAttributes = new HashMap<>();
        this.readyAttributes = new HashMap<>();
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY);
    }

    // TODO finish
    public Optional<EventCancellingComponent> checkIfComponentCancels(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        for (EventCancellingComponentAttribute eventCancellingComponentAttribute : cancellingComponents) {
            EventCancellingComponent eventCancellingComponent = eventCancellingComponentAttribute.abilityComponent();
        }
        return null;
    }

    /**
     * Checks to see if the provided {@link Event} is registered for activating this ability.
     *
     * @param event The {@link Event} to check.
     * @return {@code true} if the provided {@link Event} is registered for activating this ability.
     */
    public boolean canEventActivateAbility(@NotNull Event event) {
        return activatingAttributes.containsKey(event.getClass());
    }

    /**
     * Checks to see if the provided {@link Event} is registered for readying this ability.
     *
     * @param event The {@link Event} to check.
     * @return {@code true} if the provided {@link Event} is registered for readying this ability.
     */
    public boolean canEventReadyAbility(@NotNull Event event) {
        return readyAttributes.containsKey(event.getClass());
    }

    /**
     * Checks to see if the provided {@link Event} has any {@link EventActivatableComponent}s that fail activation.
     * <p>
     * These {@link EventActivatableComponent} are processed in order of priority and the first failure will be returned in the
     * {@link Optional}. If there are no failures, the {@link Optional} returned will be empty.
     * <p>
     * Additionally, if {@link #canEventActivateAbility(Event)} returns {@code false}, then a {@link EventNotRegisteredForActivationException} will
     * be fired.
     *
     * @param abilityHolder The {@link AbilityHolder} to check against.
     * @param event         The {@link Event} to use for checking activation components.
     * @return An {@link Optional} containing the first failing {@link EventActivatableComponent} or empty if there are no failures.
     */
    @NotNull
    public Optional<EventActivatableComponent> checkIfComponentFailsActivation(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        if (!canEventActivateAbility(event)) {
            throw new EventNotRegisteredForActivationException(event, this);
        }

        EventActivatableComponent returnComponent = null;
        for (EventActivatableComponentAttribute eventActivatableComponentAttribute : getActivatingComponents(event.getClass())) {
            EventActivatableComponent eventActivatableComponent = eventActivatableComponentAttribute.abilityComponent();

            if (!isAbilityEnabled() || !eventActivatableComponent.shouldActivate(abilityHolder, event)) {
                returnComponent = eventActivatableComponent;
                break;
            }
        }

        return Optional.ofNullable(returnComponent);
    }

    /**
     * Checks to see if the provided {@link Event} has any {@link EventReadyableComponent}s that fail readying.
     * <p>
     * These {@link EventReadyableComponent} are processed in order of priority and the first failure will be returned in the
     * {@link Optional}. If there are no failures, the {@link Optional} returned will be empty.
     * <p>
     * Additionally, if {@link #canEventReadyAbility(Event)} (Event)} returns {@code false}, then a {@link EventNotRegisteredForReadyingException} will
     * be fired.
     *
     * @param abilityHolder The {@link AbilityHolder} to check against.
     * @param event         The {@link Event} to use for checking readying components.
     * @return An {@link Optional} containing the first failing {@link EventReadyableComponent} or empty if there are no failures.
     */
    @NotNull
    public Optional<EventReadyableComponent> checkIfComponentFailsReady(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        if (!canEventReadyAbility(event)) {
            throw new EventNotRegisteredForReadyingException(event, this);
        }

        EventReadyableComponent returnComponent = null;
        Optional<ReadyData> readyData = getReadyData();
        for (EventReadyableComponentAttribute eventReadyableComponentAttribute : getReadyComponents(event.getClass())) {
            EventReadyableComponent eventReadyableComponent = eventReadyableComponentAttribute.abilityComponent();
            if (!isAbilityEnabled() || !eventReadyableComponent.shouldReady(abilityHolder, event) || readyData.isEmpty()) {
                returnComponent = eventReadyableComponent;
                break;
            }
        }
        return Optional.ofNullable(returnComponent);
    }

    // TODO
    public void addCancellingComponent(@NotNull EventCancellingComponent eventCancellingComponent, @NotNull Class<? extends Event> clazz,
                                       int priority) {
        cancellingComponents.add(new EventCancellingComponentAttribute(eventCancellingComponent, clazz, priority));
        sortCancellingComponents();
    }

    /**
     * Adds the provided {@link EventActivatableComponent} as a component for activating this ability from the provided event class. Components
     * are processed in the priority order, with the lowest priority being first. This allows chaining of components and assumptions to be made in
     * later components as the prior components would need to have passed before the later ones are processed.
     * @param eventActivatableComponent The {@link EventActivatableComponent} to register
     * @param clazz The class of the {@link Event} that can activate this ability
     * @param priority The priority of this {@link EventActivatableComponent} for this {@link Event}
     */
    public void addActivatableComponent(@NotNull EventActivatableComponent eventActivatableComponent, @NotNull Class<? extends Event> clazz,
                                        int priority) {
        List<EventActivatableComponentAttribute> newAttributes = getActivatingComponents(clazz);
        newAttributes.add(new EventActivatableComponentAttribute(eventActivatableComponent, clazz, priority));
        activatingAttributes.put(clazz, newAttributes);
        sortActivatingComponents();
    }

    /**
     * Adds the provided {@link EventReadyableComponent} as a component for readying this ability from the provided event class. Components
     * are processed in the priority order, with the lowest priority being first. This allows chaining of components and assumptions to be made in
     * later components as the prior components would need to have passed before the later ones are processed.
     * @param eventReadyableComponent The {@link EventReadyableComponent} to register
     * @param clazz The class of the {@link Event} that can ready this ability
     * @param priority The priority of this {@link EventReadyableComponent} for this {@link Event}
     */
    public void addReadyingComponent(@NotNull EventReadyableComponent eventReadyableComponent, @NotNull Class<? extends Event> clazz, int priority) {
        List<EventReadyableComponentAttribute> newAttributes = getReadyComponents(clazz);
        newAttributes.add(new EventReadyableComponentAttribute(eventReadyableComponent, clazz, priority));
        readyAttributes.put(clazz, newAttributes);
        sortReadyComponents();
    }

    @NotNull
    @Override
    public Optional<ReadyData> getReadyData() {
        return Optional.empty();
    }

    /**
     * Gets a {@link List} of all {@link EventActivatableComponentAttribute}s that are registered for the provided
     * {@link Event} cass.
     * @param clazz The {@link Event} class to get the {@link EventActivatableComponentAttribute} list of
     * @return A {@link List} of all {@link EventActivatableComponentAttribute}s that are registered for the provided
     * {@link Event} cass.
     */
    private List<EventActivatableComponentAttribute> getActivatingComponents(Class<? extends Event> clazz) {
        return activatingAttributes.getOrDefault(clazz, new ArrayList<>());
    }

    /**
     * Gets a {@link List} of all {@link EventReadyableComponentAttribute}s that are registered for the provided
     * {@link Event} cass.
     * @param clazz The {@link Event} class to get the {@link EventReadyableComponentAttribute} list of
     * @return A {@link List} of all {@link EventReadyableComponentAttribute}s that are registered for the provided
     * {@link Event} cass.
     */
    private List<EventReadyableComponentAttribute> getReadyComponents(Class<? extends Event> clazz) {
        return readyAttributes.getOrDefault(clazz, new ArrayList<>());
    }

    /**
     * Sorts all cancelling components based on their priority
     */
    private void sortCancellingComponents() {
        cancellingComponents.sort(Comparator.comparingInt(EventCancellingComponentAttribute::priority));
    }

    /**
     * Sorts all activating components based on their priority
     */
    private void sortActivatingComponents() {
        for (Class<? extends Event> clazz : activatingAttributes.keySet()) {
            List<EventActivatableComponentAttribute> attributes = getActivatingComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventActivatableComponentAttribute::priority));
        }
    }

    /**
     * Sorts all readying components based on their priority
     */
    private void sortReadyComponents() {
        for (Class<? extends Event> clazz : readyAttributes.keySet()) {
            List<EventReadyableComponentAttribute> attributes = getReadyComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventReadyableComponentAttribute::priority));
        }
    }
}
