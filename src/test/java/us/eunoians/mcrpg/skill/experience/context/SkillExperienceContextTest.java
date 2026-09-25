package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@DisplayName("SkillExperienceContext")
class SkillExperienceContextTest extends McRPGBaseTest {

    private final SkillHolder mockSkillHolder = mock(SkillHolder.class);
    private final Skill mockSkill = mock(Skill.class);
    private final Event mockEvent = mock(Event.class);

    @Nested
    @DisplayName("base experience clamping")
    class BaseExperienceClamping {

        @Test
        @DisplayName("positive base experience is preserved")
        void getBaseExperience_positiveValue_isPreserved() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, 50, mockEvent);
            assertEquals(50, context.getBaseExperience());
        }

        @Test
        @DisplayName("zero base experience is preserved")
        void getBaseExperience_zero_isPreserved() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, 0, mockEvent);
            assertEquals(0, context.getBaseExperience());
        }

        @Test
        @DisplayName("negative base experience is clamped to zero")
        void getBaseExperience_negativeValue_isClampedToZero() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, -10, mockEvent);
            assertEquals(0, context.getBaseExperience());
        }

        @Test
        @DisplayName("large negative base experience is clamped to zero")
        void getBaseExperience_largeNegativeValue_isClampedToZero() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, Integer.MIN_VALUE, mockEvent);
            assertEquals(0, context.getBaseExperience());
        }

        @Test
        @DisplayName("Integer.MAX_VALUE base experience is preserved")
        void getBaseExperience_maxValue_isPreserved() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, Integer.MAX_VALUE, mockEvent);
            assertEquals(Integer.MAX_VALUE, context.getBaseExperience());
        }
    }

    @Nested
    @DisplayName("accessors")
    class Accessors {

        @Test
        @DisplayName("getSkillHolder returns the provided skill holder")
        void getSkillHolder_returnsProvided() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, 10, mockEvent);
            assertSame(mockSkillHolder, context.getSkillHolder());
        }

        @Test
        @DisplayName("getSkill returns the provided skill")
        void getSkill_returnsProvided() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, 10, mockEvent);
            assertSame(mockSkill, context.getSkill());
        }

        @Test
        @DisplayName("getEvent returns the provided event")
        void getEvent_returnsProvided() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, 10, mockEvent);
            assertSame(mockEvent, context.getEvent());
        }
    }

    @Nested
    @DisplayName("default gain reason")
    class DefaultGainReason {

        @Test
        @DisplayName("base context returns OTHER as default gain reason")
        void getGainReason_baseContext_returnsOther() {
            MockExperienceContext context = new MockExperienceContext(mockSkillHolder, mockSkill, 10, mockEvent);
            assertEquals(McRPGGainReason.OTHER, context.getGainReason());
        }
    }

    @Nested
    @DisplayName("BlockBreakContext")
    class BlockBreakContextTests {

        @Test
        @DisplayName("returns BLOCK_BREAK as gain reason")
        void getGainReason_returnsBlockBreak() {
            BlockBreakEvent blockBreakEvent = mock(BlockBreakEvent.class);
            BlockBreakContext context = new BlockBreakContext(mockSkillHolder, mockSkill, 25, blockBreakEvent);
            assertEquals(McRPGGainReason.BLOCK_BREAK, context.getGainReason());
        }

        @Test
        @DisplayName("preserves the BlockBreakEvent")
        void getEvent_returnsBlockBreakEvent() {
            BlockBreakEvent blockBreakEvent = mock(BlockBreakEvent.class);
            BlockBreakContext context = new BlockBreakContext(mockSkillHolder, mockSkill, 25, blockBreakEvent);
            assertSame(blockBreakEvent, context.getEvent());
        }

        @Test
        @DisplayName("clamps negative experience to zero")
        void getBaseExperience_negativeValue_isClampedToZero() {
            BlockBreakEvent blockBreakEvent = mock(BlockBreakEvent.class);
            BlockBreakContext context = new BlockBreakContext(mockSkillHolder, mockSkill, -5, blockBreakEvent);
            assertEquals(0, context.getBaseExperience());
        }
    }

    @Nested
    @DisplayName("EntityDamageContext")
    class EntityDamageContextTests {

        @Test
        @DisplayName("returns ENTITY_DAMAGE as gain reason")
        void getGainReason_returnsEntityDamage() {
            EntityDamageByEntityEvent damageEvent = mock(EntityDamageByEntityEvent.class);
            EntityDamageContext context = new EntityDamageContext(mockSkillHolder, mockSkill, 30, damageEvent);
            assertEquals(McRPGGainReason.ENTITY_DAMAGE, context.getGainReason());
        }

        @Test
        @DisplayName("preserves the EntityDamageByEntityEvent")
        void getEvent_returnsEntityDamageEvent() {
            EntityDamageByEntityEvent damageEvent = mock(EntityDamageByEntityEvent.class);
            EntityDamageContext context = new EntityDamageContext(mockSkillHolder, mockSkill, 30, damageEvent);
            assertSame(damageEvent, context.getEvent());
        }

        @Test
        @DisplayName("clamps negative experience to zero")
        void getBaseExperience_negativeValue_isClampedToZero() {
            EntityDamageByEntityEvent damageEvent = mock(EntityDamageByEntityEvent.class);
            EntityDamageContext context = new EntityDamageContext(mockSkillHolder, mockSkill, -100, damageEvent);
            assertEquals(0, context.getBaseExperience());
        }
    }
}
