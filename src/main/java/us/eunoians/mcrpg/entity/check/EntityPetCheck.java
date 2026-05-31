package us.eunoians.mcrpg.entity.check;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

//TODO javadoc (https://github.com/DiamondDagger590/McRPG/issues/180)
public interface EntityPetCheck {

    boolean isEntityPetOf(@NotNull Entity entity1, @NotNull Entity entity2);
}
