package us.eunoians.mcrpg.ability.impl.swords.bleed;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.ability.component.activatable.OnAttackComponent;
import us.eunoians.mcrpg.ability.component.activatable.TargetablePlayerComponent;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Random;

/**
 * Contains all the {@link us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent} that
 * can activate Bleed.
 */
public class BleedComponents {

    private static final Random RANDOM = new Random();
    public static final BleedOnAttackComponent BLEED_ON_ATTACK_COMPONENT = new BleedOnAttackComponent();
    public static final BleedOnTargetPlayerComponent BLEED_ON_TARGET_PLAYER_COMPONENT = new BleedOnTargetPlayerComponent();
//    public static final BleedEligibleForTargetComponent BLEED_ELIGIBLE_FOR_TARGET_COMPONENT = new BleedEligibleForTargetComponent();

    private static class BleedOnAttackComponent implements OnAttackComponent {
        @Override
        public boolean affectsEntity(@NotNull Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (!OnAttackComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }
            // Get the activation boost from serrated strikes
            double activationBoost = 0;
            if (abilityHolder.isAbilityActive(SerratedStrikes.SERRATED_STRIKES_KEY)) {
                SerratedStrikes serratedStrikes = (SerratedStrikes) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(SerratedStrikes.SERRATED_STRIKES_KEY);
                activationBoost = serratedStrikes.getBoostToBleedActivation(serratedStrikes.getCurrentAbilityTier(abilityHolder));
            }
            // Check if they're a skill holder, if so then check the activation equation. Otherwise activate it ig (needs custom handling in the future for bosses n stuff)
            if (abilityHolder instanceof SkillHolder skillHolder) {
                Bleed bleed = (Bleed) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(Bleed.BLEED_KEY);
                var skillHolderDataOptional = skillHolder.getSkillHolderData(Swords.SWORDS_KEY);
                if (skillHolderDataOptional.isPresent()) {
                    return (bleed.getActivationChance(skillHolder) + activationBoost) * 1000 > RANDOM.nextInt(100000);
                }
            }
            return false;
        }
    }

    private static class BleedOnTargetPlayerComponent implements TargetablePlayerComponent {

        @Override
        public boolean affectAllies() {
            return true;
        }

        @Override
        public boolean affectEnemies() {
            return true;
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent
                    && entityDamageByEntityEvent.getDamager() instanceof Player damager
                    && entityDamageByEntityEvent.getEntity() instanceof Player damaged) {
                return doesAffect(damager, damaged);
            }
            return true;
        }
    }

    public static class BleedEligibleForTargetComponent implements EventActivatableComponent {

        private final McRPG mcRPG;

        public BleedEligibleForTargetComponent(@NotNull McRPG mcRPG) {
            this.mcRPG = mcRPG;
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent && entityDamageByEntityEvent.getEntity() instanceof LivingEntity livingEntity) {
                return mcRPG.getBleedManager().canEntityStartBleeding(livingEntity);
            }
            return false;
        }
    }
}
