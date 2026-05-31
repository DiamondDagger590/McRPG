package us.eunoians.mcrpg.listener.ability;

import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.MobCastableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;

import java.util.UUID;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link OnMobAbilityTriggerListener}.
 * <p>
 * The handler is invoked directly (not through Bukkit's event system) to avoid
 * listener accumulation issues in MockBukkit across tests.
 */
public class OnMobAbilityTriggerListenerTest extends McRPGBaseTest {

    private final OnMobAbilityTriggerListener listener = new OnMobAbilityTriggerListener();

    @Test
    public void handleMobAbilityTrigger_callsMobActivateForMobCastableAbility() {
        MobCastableAbility ability = mock(MobCastableAbility.class);
        AbilityHolder holder = new AbilityHolder(mcRPG, UUID.randomUUID());
        LivingEntity caster = mock(LivingEntity.class);
        LivingEntity target = mock(LivingEntity.class);

        MobAbilityTriggerEvent event = new MobAbilityTriggerEvent(holder, ability, caster, target);
        listener.handleMobAbilityTrigger(event);

        verify(ability).mobActivate(eq(holder), eq(event));
        verifyNoMoreInteractions(ability);
    }

    @Test
    public void handleMobAbilityTrigger_skipsNonMobCastableAbility() {
        Ability ability = mock(Ability.class);
        AbilityHolder holder = new AbilityHolder(mcRPG, UUID.randomUUID());
        LivingEntity caster = mock(LivingEntity.class);
        LivingEntity target = mock(LivingEntity.class);

        MobAbilityTriggerEvent event = new MobAbilityTriggerEvent(holder, ability, caster, target);
        listener.handleMobAbilityTrigger(event);

        verifyNoInteractions(ability);
    }
}
