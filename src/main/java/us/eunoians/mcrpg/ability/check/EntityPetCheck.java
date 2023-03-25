package us.eunoians.mcrpg.ability.check;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

//TODO javadoc
public interface EntityPetCheck {

    public boolean isEntityPetOf(@NotNull Entity entity1, @NotNull Entity entity2);
}
