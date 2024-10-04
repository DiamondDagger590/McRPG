package us.eunoians.mcrpg.skill.impl.swords;

import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.McRPGSkill;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * A {@link Skill} that focuses on the usage of Minecraft Swords.
 * <p>
 * Players will gain experience by attacking mobs with swords and unlock abilities focused
 * on the {@link us.eunoians.mcrpg.ability.impl.swords.Bleed} mechanic.
 */
public final class Swords extends McRPGSkill {

    public static final NamespacedKey SWORDS_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "swords");

    public Swords() {
        super(SWORDS_KEY);
        addLevelableComponent(SwordsSkillComponents.SWORDS_LEVEL_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Swords";
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }
}
