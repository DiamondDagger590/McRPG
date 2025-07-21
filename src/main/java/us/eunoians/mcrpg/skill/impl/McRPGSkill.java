package us.eunoians.mcrpg.skill.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.skill.BaseSkill;

import java.util.Optional;

/**
 * A type of skill that is used by native McRPG skills.
 */
public abstract class McRPGSkill extends BaseSkill {

    public McRPGSkill(@NotNull NamespacedKey skillKey) {
        super(skillKey);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
