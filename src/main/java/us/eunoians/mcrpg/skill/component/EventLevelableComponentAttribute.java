package us.eunoians.mcrpg.skill.component;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public record EventLevelableComponentAttribute (@NotNull EventLevelableComponent levelableComponent,
                                                @NotNull Class<? extends Event> clazz,
                                                int priority) {
}
