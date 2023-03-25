package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.component.OnAttackAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class Bleed extends Ability implements OnAttackAbility {

    private static final NamespacedKey BLEED_KEY = new NamespacedKey(McRPG.getInstance(), "bleed");

    public Bleed() {
        super(BLEED_KEY);
    }

    @Override
    public void playActivationNoise() {

    }

    @Override
    public void playActivationParticle() {

    }

    @Override
    public void activate(@NotNull AbilityHolder abilityHolder, Object... data) {
        Bukkit.broadcastMessage("1");
    }

    @Override
    public boolean affectsEntity(@NotNull Entity entity) {
        return entity instanceof LivingEntity;
    }

    @Override
    public boolean shouldActivateOnAttack(@NotNull EntityDamageByEntityEvent entityDamageByEntityEvent) {
        return OnAttackAbility.super.shouldActivateOnAttack(entityDamageByEntityEvent);
    }
}
