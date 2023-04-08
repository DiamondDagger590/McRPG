package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class Bleed extends Ability {

    private static final NamespacedKey BLEED_KEY = new NamespacedKey(McRPG.getInstance(), "bleed");

    public Bleed() {
        super(BLEED_KEY);
        addActivatableComponent(BleedComponents.BLEED_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
        addActivatableComponent(BleedComponents.BLEED_ON_TARGET_PLAYER_COMPONENT, EntityDamageByEntityEvent.class, 1);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        Bukkit.broadcastMessage("Activated");
    }
}
