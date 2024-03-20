package us.eunoians.mcrpg.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.skill.component.EventLevelableComponent;
import us.eunoians.mcrpg.skill.component.EventLevelableComponentAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base class for any skills
 */
public abstract class Skill {

    private final Map<Class<? extends Event>, List<EventLevelableComponentAttribute>> levelingAttributes;

    private final NamespacedKey skillKey;

    public Skill(@NotNull NamespacedKey skillKey) {
        this.skillKey = skillKey;
        this.levelingAttributes = new HashMap<>();
    }

    /**
     * Gets the {@link NamespacedKey} that represents this skill
     *
     * @return The {@link NamespacedKey} that represents this skill
     */
    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    /**
     * Gets the name of this skill to display to the player in messages or guis.
     *
     * @return The name of this skill to display to the player in messages or guis
     */
    @NotNull
    public abstract String getDisplayName();

    /**
     * Gets a {@link List} of all {@link EventLevelableComponentAttribute}s used by this skill that trigger based on the
     * {@link Event} passed in.
     *
     * @param clazz The {@link Event} to get the {@link EventLevelableComponentAttribute}s for.
     * @return A {@link List} of all {@link EventLevelableComponentAttribute}s used by this skill that trigger based on the
     * {@link Event} passed in.
     * <p>
     * This list will be empty if there are no {@link EventLevelableComponentAttribute}s that this skill has for the provided
     * {@link Event}.
     */
    @NotNull
    public List<EventLevelableComponentAttribute> getLevelableComponents(@NotNull Class<? extends Event> clazz) {
        return levelingAttributes.getOrDefault(clazz, new ArrayList<>());
    }

    /**
     * Checks to see if the provided {@link Event} can be used to provide experience to this skill.
     *
     * @param event The {@link Event} to check.
     * @return {@code true} if the provided {@link Event} can be used to provide experience to this skill.
     */
    public boolean canEventLevelSkill(@NotNull Event event) {
        return levelingAttributes.containsKey(event.getClass());
    }

    /**
     * Calculates the amount of experience to award the provided {@link SkillHolder} based on the provided {@link Event}.
     * <p>
     * No experience is awarded during this method call, this method simply calculates the amount of experience that the {@link Event}
     * CAN award.
     * <p>
     * This method looks through every {@link EventLevelableComponentAttribute} that is used for the {@link Event} (in sorted order of priority),
     * and will return the highest calculated experience gain from any of these attributes.
     * <p>
     * If one {@link EventLevelableComponentAttribute} returns {@code false} on {@link EventLevelableComponent#shouldGiveExperience(SkillHolder, Event)},
     * then it will stop checking the subsequent ones and return a value of {@code 0}. This allows for other plugins to add additional {@link EventLevelableComponentAttribute}s
     * to vanilla McRPG skills, while allowing the default experience checks to do validation on experience awarding.
     *
     * @param skillHolder The {@link SkillHolder} to calculate experience for.
     * @param event       The {@link Event} to calculate experience from.
     * @return The non-negative, zero inclusive amount of experience that can be awarded by the provided event.
     */
    public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        // If the event can't be used to level this skill, throw an error because for some reason we are expecting it to be and it isn't.
        if (!canEventLevelSkill(event)) {
            throw new EventNotRegisteredForLevelingException(event, this);
        }

        int expToAward = 0;
        // Check every levelable component for experience amounts
        for (EventLevelableComponentAttribute eventLevelableComponentAttribute : getLevelableComponents(event.getClass())) {
            EventLevelableComponent eventLevelableComponent = eventLevelableComponentAttribute.levelableComponent();
            /*
             If this component can't give experience, then stop iterating and return 0.

             We assume that if a component CAN give experience but doesn't want to, it will return 0 when we calculate.
             This check here is to validate whether conditions are right to award experience in the first place, as we assume
             subsequent components will share at least the same criteria (they may have more specific criteria but should never
             be more broad than the ones before it).
             */
            if (!eventLevelableComponent.shouldGiveExperience(skillHolder, event)) {
                return 0;
            } else {
                // Set the amount of experience to return if it is higher than the current result.
                int exp = eventLevelableComponent.calculateExperienceToGive(skillHolder, event);
                if (exp > expToAward) {
                    expToAward = exp;
                }
            }
        }

        return expToAward;
    }

    /**
     * Adds the provided {@link EventLevelableComponent} as an option to award experience whenever {@link #calculateExperienceToGive(SkillHolder, Event)}
     * with the provided {@link Event}.
     * <p>
     * {@link EventLevelableComponent}s use a priority system. These start at 0 (McRPG level) and go from there. If there are two components that
     * share the same priority, then order is not guaranteed. The order allows for setting specific validation criteria that other components following should
     * be encompassed by. An example being a component with priority 0 validating that the player is holding a sword in order to award swords experience.
     * Since all other components that award swords experience should be encompassed by this same criteria, priority 0 makes sense for this check.
     *
     * @param eventLevelableComponent The {@link EventLevelableComponent} to add as an option
     * @param clazz The {@link Event} class that the component is being registered against
     * @param priority The priority of this component (starts at 0 and goes up from there).
     */
    public void addLevelableComponent(@NotNull EventLevelableComponent eventLevelableComponent, @NotNull Class<? extends Event> clazz,
                                      int priority) {
        List<EventLevelableComponentAttribute> newAttributes = getLevelableComponents(clazz);
        newAttributes.add(new EventLevelableComponentAttribute(eventLevelableComponent, clazz, priority));
        levelingAttributes.put(clazz, newAttributes);
        sortLevelingComponents();
    }

    /**
     * Sorts the leveling components based on priority.
     */
    private void sortLevelingComponents() {
        for (Class<? extends Event> clazz : levelingAttributes.keySet()) {
            List<EventLevelableComponentAttribute> attributes = getLevelableComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventLevelableComponentAttribute::priority));
        }
    }
}
