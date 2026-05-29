package us.eunoians.mcrpg.event.ability;

import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.MockAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link MobAbilityTriggerEvent}.
 */
public class MobAbilityTriggerEventTest extends McRPGBaseTest {

    private Ability ability;
    private AbilityHolder holder;
    private LivingEntity caster;
    private LivingEntity target;

    @BeforeEach
    public void setup() {
        ability = new MockAbility(mcRPG);
        holder = new AbilityHolder(mcRPG, UUID.randomUUID());
        caster = mock(LivingEntity.class);
        target = mock(LivingEntity.class);
    }

    @Test
    public void constructor_setsAbilityAndHolder() {
        MobAbilityTriggerEvent event = new MobAbilityTriggerEvent(holder, ability, caster, target);

        assertEquals(ability, event.getAbility());
        assertEquals(holder, event.getAbilityHolder());
    }

    @Test
    public void getCaster_returnsCasterEntity() {
        MobAbilityTriggerEvent event = new MobAbilityTriggerEvent(holder, ability, caster, target);

        assertEquals(caster, event.getCaster());
    }

    @Test
    public void getTarget_returnsTargetEntity() {
        MobAbilityTriggerEvent event = new MobAbilityTriggerEvent(holder, ability, caster, target);

        assertEquals(target, event.getTarget());
    }

    @Test
    public void getHandlerList_isNotNull() {
        assertNotNull(MobAbilityTriggerEvent.getHandlerList());
    }

    @Test
    public void getHandlers_returnsSameListAsStaticMethod() {
        MobAbilityTriggerEvent event = new MobAbilityTriggerEvent(holder, ability, caster, target);

        assertSame(MobAbilityTriggerEvent.getHandlerList(), event.getHandlers());
    }
}
