package us.eunoians.mcrpg.ability.impl.type;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.ability.unlock.builtin.SkillLevelUnlockConditionType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class TierableAbilityUnlockConditionDefaultTest extends McRPGBaseTest {

    private static final NamespacedKey SWORDS = new NamespacedKey("mcrpg", "swords");

    @DisplayName("Given a non-SkillAbility tierable, when getDefaultUnlockConditions, then it returns empty")
    @Test
    public void defaultConditions_emptyWhenNotSkillAbility() {
        StubTierableAbility ability = new StubTierableAbility(mock(Plugin.class),
                new NamespacedKey("mcrpg", "non_skill_tierable"));
        assertTrue(ability.getDefaultUnlockConditions().isEmpty());
    }

    @DisplayName("Given a SkillAbility tierable, when getDefaultUnlockConditions, then it returns one SkillLevel condition at tier-1 level")
    @Test
    public void defaultConditions_returnsSkillLevelForSkillAbility() {
        SkillAwareStubTierableAbility ability = new SkillAwareStubTierableAbility(mock(Plugin.class),
                new NamespacedKey("mcrpg", "vampire"), SWORDS, 250);
        List<UnlockConditionType> defaults = ability.getDefaultUnlockConditions();
        assertEquals(1, defaults.size());
        SkillLevelUnlockConditionType condition = assertInstanceOf(SkillLevelUnlockConditionType.class, defaults.get(0));
        assertEquals(SWORDS, condition.getSkillKey());
        assertEquals(250, condition.getRequiredLevel());
    }

    /**
     * Test-only tierable that also declares a skill — exercises the
     * {@link TierableAbility#getDefaultUnlockConditions()} {@code SkillAbility} branch.
     */
    private static final class SkillAwareStubTierableAbility extends StubTierableAbility implements SkillAbility {

        private final NamespacedKey skillKey;
        private final int tier1Level;

        SkillAwareStubTierableAbility(Plugin plugin, NamespacedKey key, NamespacedKey skillKey, int tier1Level) {
            super(plugin, key);
            this.skillKey = skillKey;
            this.tier1Level = tier1Level;
        }

        @NotNull
        @Override
        public NamespacedKey getSkillKey() {
            return skillKey;
        }

        @Override
        public int getUnlockLevelForTier(int tier) {
            return tier == 1 ? tier1Level : tier1Level * tier;
        }
    }
}
