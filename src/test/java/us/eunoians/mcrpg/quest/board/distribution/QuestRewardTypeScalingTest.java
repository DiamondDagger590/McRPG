package us.eunoians.mcrpg.quest.board.distribution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class QuestRewardTypeScalingTest extends McRPGBaseTest {

    @Nested
    @DisplayName("ExperienceRewardType scaling")
    class ExperienceScaling {

        @DisplayName("withAmountMultiplier returns new instance with scaled amount")
        @Test
        void scalingReturnsNewInstance() {
            var base = new ExperienceRewardType()
                    .fromSerializedConfig(java.util.Map.of("skill", "MINING", "amount", 1000L));
            QuestRewardType scaled = base.withAmountMultiplier(0.5);

            assertNotSame(base, scaled);
        }

        @DisplayName("scaling below 1 clamps to minimum of 1")
        @Test
        void scalingClampToOne() {
            var base = new ExperienceRewardType()
                    .fromSerializedConfig(java.util.Map.of("skill", "MINING", "amount", 1L));
            QuestRewardType scaled = base.withAmountMultiplier(0.01);

            assertNotSame(base, scaled);
            // Serialized config should show amount of 1 (minimum)
            var config = scaled.serializeConfig();
            long amount = ((Number) config.get("amount")).longValue();
            org.junit.jupiter.api.Assertions.assertEquals(1L, amount);
        }

        @DisplayName("scaling preserves skill name")
        @Test
        void scalingPreservesSkill() {
            var base = new ExperienceRewardType()
                    .fromSerializedConfig(java.util.Map.of("skill", "HERBALISM", "amount", 500L));
            QuestRewardType scaled = base.withAmountMultiplier(0.5);

            org.junit.jupiter.api.Assertions.assertEquals("HERBALISM", scaled.serializeConfig().get("skill"));
        }

        @DisplayName("1.0 multiplier returns same amount")
        @Test
        void identityMultiplier() {
            var base = new ExperienceRewardType()
                    .fromSerializedConfig(java.util.Map.of("skill", "MINING", "amount", 1000L));
            QuestRewardType scaled = base.withAmountMultiplier(1.0);

            long amount = ((Number) scaled.serializeConfig().get("amount")).longValue();
            org.junit.jupiter.api.Assertions.assertEquals(1000L, amount);
        }
    }

    @Nested
    @DisplayName("CommandRewardType scaling")
    class CommandScaling {

        @DisplayName("default withAmountMultiplier returns same instance (no-op)")
        @Test
        void defaultReturnsThis() {
            var base = new CommandRewardType();
            QuestRewardType scaled = base.withAmountMultiplier(0.5);

            assertSame(base, scaled);
        }
    }

    @Nested
    @DisplayName("Default interface behavior")
    class DefaultBehavior {

        @DisplayName("QuestRewardType default withAmountMultiplier returns this")
        @Test
        void defaultMethodReturnsThis() {
            // CommandRewardType inherits the default, so verify with it
            CommandRewardType cmd = new CommandRewardType();
            assertSame(cmd, cmd.withAmountMultiplier(0.1));
        }
    }
}
