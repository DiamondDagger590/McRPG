package us.eunoians.mcrpg.ability.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponentAttribute;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponent;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponentAttribute;
import us.eunoians.mcrpg.ability.component.readyable.EventReadyableComponent;
import us.eunoians.mcrpg.ability.component.readyable.EventReadyableComponentAttribute;
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
public abstract class BaseAbility implements Ability {

    private final NamespacedKey abilityKey;
    private final List<EventCancellingComponentAttribute> cancellingComponents;
    private final Map<Class<? extends Event>, List<EventActivatableComponentAttribute>> activatingAttributes;
    private final Map<Class<? extends Event>, List<EventReadyableComponentAttribute>> readyAttributes;

    public BaseAbility(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
        this.cancellingComponents = new ArrayList<>();
        this.activatingAttributes = new HashMap<>();
        this.readyAttributes = new HashMap<>();
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

    public boolean canEventActivateAbility(@NotNull Event event) {
        return activatingAttributes.containsKey(event.getClass());
    }

    public boolean canEventReadyAbility(@NotNull Event event) {
        return readyAttributes.containsKey(event.getClass());
    }

    @NotNull
    public Optional<EventActivatableComponent> checkIfComponentFailsActivation(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        if (!canEventActivateAbility(event)) {
            throw new EventNotRegisteredForActivation(event, this);
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

    @NotNull
    public Optional<EventReadyableComponent> checkIfComponentFailsReady(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        if (!canEventReadyAbility(event)) {
            throw new EventNotRegisteredForActivation(event, this);
        }

        EventReadyableComponent returnComponent = null;
        for (EventReadyableComponentAttribute eventReadyableComponentAttribute : getReadyComponents(event.getClass())) {
            EventReadyableComponent eventReadyableComponent = eventReadyableComponentAttribute.abilityComponent();
            if (!isAbilityEnabled() || !eventReadyableComponent.shouldReady(abilityHolder, event)) {
                returnComponent = eventReadyableComponent;
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

    public void addReadyingComponent(@NotNull EventReadyableComponent eventReadyableComponent, @NotNull Class<? extends Event> clazz, int priority) {
        List<EventReadyableComponentAttribute> newAttributes = getReadyComponents(clazz);
        newAttributes.add(new EventReadyableComponentAttribute(eventReadyableComponent, clazz, priority));
        readyAttributes.put(clazz, newAttributes);
        sortReadyComponents();
    }

    private List<EventActivatableComponentAttribute> getActivatingComponents(Class<? extends Event> clazz) {
        return activatingAttributes.getOrDefault(clazz, new ArrayList<>());
    }

    private List<EventReadyableComponentAttribute> getReadyComponents(Class<? extends Event> clazz) {
        return readyAttributes.getOrDefault(clazz, new ArrayList<>());
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

    private void sortReadyComponents() {
        for(Class<? extends Event> clazz : readyAttributes.keySet()) {
            List<EventReadyableComponentAttribute> attributes = getReadyComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventReadyableComponentAttribute::priority));
        }
    }
}
