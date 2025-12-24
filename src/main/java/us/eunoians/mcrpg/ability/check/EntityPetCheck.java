package us.eunoians.mcrpg.ability.check;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

//TODO javadoc (https://github.com/DiamondDagger590/McRPG/issues/180)
public interface EntityPetCheck {

    public boolean isEntityPetOf(@NotNull Entity entity1, @NotNull Entity entity2);
}
