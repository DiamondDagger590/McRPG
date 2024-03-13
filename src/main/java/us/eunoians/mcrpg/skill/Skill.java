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

//TODO javadoc
public abstract class Skill {

    private final Map<Class<? extends Event>, List<EventLevelableComponentAttribute>> levelingAttributes;

    private final NamespacedKey skillKey;

    public Skill(@NotNull NamespacedKey skillKey) {
        this.skillKey = skillKey;
        this.levelingAttributes = new HashMap<>();
    }

    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    @NotNull
    public abstract String getDisplayName();

    @NotNull
    public List<EventLevelableComponentAttribute> getLevelableComponents(@NotNull Class<? extends Event> clazz) {
        return levelingAttributes.getOrDefault(clazz, new ArrayList<>());
    }

    public boolean canEventLevelSkill(@NotNull Event event) {
        return levelingAttributes.containsKey(event.getClass());
    }

    public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {

        if (!canEventLevelSkill(event)) {
            throw new EventNotRegisteredForLevelingException(event, this);
        }

        int expToAward = 0;
        for (EventLevelableComponentAttribute eventLevelableComponentAttribute : getLevelableComponents(event.getClass())) {
            EventLevelableComponent eventLevelableComponent = eventLevelableComponentAttribute.levelableComponent();

            if (!eventLevelableComponent.shouldGiveExperience(skillHolder, event)) {
                return 0;
            }
            else {
                int exp = eventLevelableComponent.calculateExperienceToGive(skillHolder, event);
                if (exp > expToAward) {
                    expToAward = exp;
                }
            }
        }

        return expToAward;
    }

    public void addLevelableComponent(@NotNull EventLevelableComponent eventLevelableComponent, @NotNull Class<? extends Event> clazz,
                                      int priority) {
        List<EventLevelableComponentAttribute> newAttributes = getLevelableComponents(clazz);
        newAttributes.add(new EventLevelableComponentAttribute(eventLevelableComponent, clazz, priority));
        levelingAttributes.put(clazz, newAttributes);
        sortActivatingComponents();
    }

    private void sortActivatingComponents() {
        for (Class<? extends Event> clazz : levelingAttributes.keySet()) {
            List<EventLevelableComponentAttribute> attributes = getLevelableComponents(clazz);
            attributes.sort(Comparator.comparingInt(EventLevelableComponentAttribute::priority));
        }
    }
}
