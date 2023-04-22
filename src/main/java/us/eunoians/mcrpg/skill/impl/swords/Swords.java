package us.eunoians.mcrpg.skill.impl.swords;

import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.Skill;

public class Swords extends Skill {

    private static final NamespacedKey SWORDS_KEY = new NamespacedKey(McRPG.getInstance(), "swords");

    public Swords() {
        super(SWORDS_KEY);
        addLevelableComponent(SwordsComponents.SWORDS_LEVEL_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
    }


}
