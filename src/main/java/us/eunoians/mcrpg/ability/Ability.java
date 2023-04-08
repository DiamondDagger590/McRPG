package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponentAttribute;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponent;
import us.eunoians.mcrpg.ability.component.cancel.EventCancellingComponentAttribute;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.exception.EventNotRegisteredForActivation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An ability doesn't always belong to a skill, while a skill will always have abilities
 * tied to it.
 */
public abstract class Ability implements Listener {

    private final NamespacedKey abilityKey;
    private final List<EventCancellingComponentAttribute> cancellingComponents;
    private final Map<Class<? extends Event>, List<EventActivatableComponentAttribute>> activatingAttributes;

    public Ability(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
        this.cancellingComponents = new ArrayList<>();
        this.activatingAttributes = new HashMap<>();
    }

    //TODO javadoc
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    public abstract void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event);

    //TODO finish
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

        if(!canEventActivateAbility(event)) {
            throw new EventNotRegisteredForActivation(event, this);
        }

        EventActivatableComponent returnComponent = null;
        for (EventActivatableComponentAttribute eventActivatableComponentAttribute : getActivatingComponents(event.getClass())) {
            EventActivatableComponent eventActivatableComponent = eventActivatableComponentAttribute.abilityComponent();

            if(!eventActivatableComponent.shouldActivate(abilityHolder, event)) {
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
        for(Class<? extends Event> clazz : activatingAttributes.keySet()) {
            List<EventActivatableComponentAttribute> attributes = getActivatingComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventActivatableComponentAttribute::priority));
        }
    }
}
