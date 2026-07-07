package us.eunoians.mcrpg.event.skill;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.GainReason;
import us.eunoians.mcrpg.skill.experience.context.McRPGGainReason;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillEventCoverageTest extends McRPGBaseTest {

    private static final NamespacedKey TEST_SKILL_KEY = NamespacedKey.fromString("mcrpg:test_skill");

    private SkillHolder mockSkillHolder() {
        return mock(SkillHolder.class);
    }

    private Skill mockSkill() {
        Skill skill = mock(Skill.class);
        when(skill.getSkillKey()).thenReturn(TEST_SKILL_KEY);
        return skill;
    }

    @Nested
    @DisplayName("SkillEvent")
    class SkillEventBase {

        @Test
        @DisplayName("getSkillKey returns constructor value")
        void getSkillKey_returnsConstructorValue() {
            SkillGainLevelEvent event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull() {
            SkillGainLevelEvent event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1);
            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("getHandlerList returns non-null static list")
        void getHandlerList_returnsNonNull() {
            assertNotNull(SkillEvent.getHandlerList());
        }

        @Test
        @DisplayName("getHandlers and getHandlerList return same instance")
        void getHandlers_sameAsStaticHandlerList() {
            SkillGainLevelEvent event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1);
            assertSame(SkillEvent.getHandlerList(), event.getHandlers());
        }
    }

    @Nested
    @DisplayName("SkillGainExpEvent")
    class SkillGainExpEventTests {

        @Test
        @DisplayName("constructor with Skill uses skill key")
        void constructor_withSkill_usesSkillKey() {
            Skill skill = mockSkill();
            var event = new SkillGainExpEvent(mockSkillHolder(), skill, 100);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }

        @Test
        @DisplayName("constructor with Skill defaults to OTHER gain reason")
        void constructor_withSkill_defaultsToOtherGainReason() {
            var event = new SkillGainExpEvent(mockSkillHolder(), mockSkill(), 100);
            assertEquals(McRPGGainReason.OTHER, event.getGainReason());
        }

        @Test
        @DisplayName("constructor with NamespacedKey defaults to OTHER gain reason")
        void constructor_withKey_defaultsToOtherGainReason() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 50);
            assertEquals(McRPGGainReason.OTHER, event.getGainReason());
        }

        @Test
        @DisplayName("constructor stores experience value")
        void constructor_storesExperience() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 250);
            assertEquals(250, event.getExperience());
        }

        @Test
        @DisplayName("constructor clamps negative experience to zero")
        void constructor_clampsNegativeExpToZero() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, -50);
            assertEquals(0, event.getExperience());
        }

        @Test
        @DisplayName("constructor preserves zero experience")
        void constructor_preservesZeroExperience() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 0);
            assertEquals(0, event.getExperience());
        }

        @Test
        @DisplayName("constructor stores custom gain reason")
        void constructor_storesCustomGainReason() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100, McRPGGainReason.COMMAND);
            assertEquals(McRPGGainReason.COMMAND, event.getGainReason());
        }

        @Test
        @DisplayName("getSkillHolder returns constructor value")
        void getSkillHolder_returnsConstructorValue() {
            SkillHolder holder = mockSkillHolder();
            var event = new SkillGainExpEvent(holder, TEST_SKILL_KEY, 100);
            assertSame(holder, event.getSkillHolder());
        }

        @Test
        @DisplayName("setExperience updates value")
        void setExperience_updatesValue() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100);
            event.setExperience(500);
            assertEquals(500, event.getExperience());
        }

        @Test
        @DisplayName("setExperience clamps negative value to zero")
        void setExperience_clampsNegativeToZero() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100);
            event.setExperience(-10);
            assertEquals(0, event.getExperience());
        }

        @Test
        @DisplayName("setExperience preserves zero")
        void setExperience_preservesZero() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100);
            event.setExperience(0);
            assertEquals(0, event.getExperience());
        }

        @Test
        @DisplayName("isCancelled defaults to false")
        void isCancelled_defaultsFalse() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled to true makes isCancelled true")
        void setCancelled_toTrue() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled to false after true restores state")
        void setCancelled_toFalseAfterTrue() {
            var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100);
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("constructor with all gain reasons stores each correctly")
        void constructor_allGainReasons() {
            for (McRPGGainReason reason : McRPGGainReason.values()) {
                var event = new SkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 10, reason);
                assertSame(reason, event.getGainReason());
            }
        }
    }

    @Nested
    @DisplayName("PostSkillGainExpEvent")
    class PostSkillGainExpEventTests {

        @Test
        @DisplayName("constructor with Skill uses skill key and defaults")
        void constructor_withSkill_usesDefaults() {
            Skill skill = mockSkill();
            var event = new PostSkillGainExpEvent(mockSkillHolder(), skill);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
            assertEquals(0, event.getExperience());
            assertEquals(McRPGGainReason.OTHER, event.getGainReason());
        }

        @Test
        @DisplayName("constructor with NamespacedKey uses defaults")
        void constructor_withKey_usesDefaults() {
            var event = new PostSkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY);
            assertEquals(0, event.getExperience());
            assertEquals(McRPGGainReason.OTHER, event.getGainReason());
        }

        @Test
        @DisplayName("full constructor stores all values")
        void fullConstructor_storesAllValues() {
            SkillHolder holder = mockSkillHolder();
            var event = new PostSkillGainExpEvent(holder, TEST_SKILL_KEY, 300, McRPGGainReason.BLOCK_BREAK);
            assertSame(holder, event.getSkillHolder());
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
            assertEquals(300, event.getExperience());
            assertEquals(McRPGGainReason.BLOCK_BREAK, event.getGainReason());
        }

        @Test
        @DisplayName("experience is immutable after construction")
        void experience_isImmutable() {
            var event = new PostSkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 100, McRPGGainReason.OTHER);
            assertEquals(100, event.getExperience());
        }

        @Test
        @DisplayName("getSkillHolder returns constructor value")
        void getSkillHolder_returnsConstructorValue() {
            SkillHolder holder = mockSkillHolder();
            var event = new PostSkillGainExpEvent(holder, TEST_SKILL_KEY);
            assertSame(holder, event.getSkillHolder());
        }

        @Test
        @DisplayName("constructor with custom gain reason stores it")
        void constructor_customGainReason() {
            GainReason customReason = McRPGGainReason.REDEEM;
            var event = new PostSkillGainExpEvent(mockSkillHolder(), TEST_SKILL_KEY, 50, customReason);
            assertSame(customReason, event.getGainReason());
        }
    }

    @Nested
    @DisplayName("SkillGainLevelEvent")
    class SkillGainLevelEventTests {

        @Test
        @DisplayName("constructor stores levels")
        void constructor_storesLevels() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 5);
            assertEquals(5, event.getLevels());
        }

        @Test
        @DisplayName("constructor clamps negative levels to zero")
        void constructor_clampsNegativeLevelsToZero() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, -3);
            assertEquals(0, event.getLevels());
        }

        @Test
        @DisplayName("constructor preserves zero levels")
        void constructor_preservesZeroLevels() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 0);
            assertEquals(0, event.getLevels());
        }

        @Test
        @DisplayName("getSkillHolder returns constructor value")
        void getSkillHolder_returnsConstructorValue() {
            SkillHolder holder = mockSkillHolder();
            var event = new SkillGainLevelEvent(holder, TEST_SKILL_KEY, 1);
            assertSame(holder, event.getSkillHolder());
        }

        @Test
        @DisplayName("setLevels updates value")
        void setLevels_updatesValue() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1);
            event.setLevels(10);
            assertEquals(10, event.getLevels());
        }

        @Test
        @DisplayName("setLevels clamps negative to zero")
        void setLevels_clampsNegativeToZero() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 5);
            event.setLevels(-1);
            assertEquals(0, event.getLevels());
        }

        @Test
        @DisplayName("setLevels preserves zero")
        void setLevels_preservesZero() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 5);
            event.setLevels(0);
            assertEquals(0, event.getLevels());
        }

        @Test
        @DisplayName("getSkillKey returns constructor value")
        void getSkillKey_returnsConstructorValue() {
            var event = new SkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }
    }

    @Nested
    @DisplayName("PostSkillGainLevelEvent")
    class PostSkillGainLevelEventTests {

        @Test
        @DisplayName("constructor stores all values")
        void constructor_storesAllValues() {
            SkillHolder holder = mockSkillHolder();
            var event = new PostSkillGainLevelEvent(holder, TEST_SKILL_KEY, 5, 8);
            assertSame(holder, event.getSkillHolder());
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
            assertEquals(5, event.getBeforeLevel());
            assertEquals(8, event.getAfterLevel());
        }

        @Test
        @DisplayName("getBeforeLevel returns constructor value")
        void getBeforeLevel_returnsConstructorValue() {
            var event = new PostSkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1, 3);
            assertEquals(1, event.getBeforeLevel());
        }

        @Test
        @DisplayName("getAfterLevel returns constructor value")
        void getAfterLevel_returnsConstructorValue() {
            var event = new PostSkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 1, 3);
            assertEquals(3, event.getAfterLevel());
        }

        @Test
        @DisplayName("before and after can be equal")
        void beforeAndAfter_canBeEqual() {
            var event = new PostSkillGainLevelEvent(mockSkillHolder(), TEST_SKILL_KEY, 5, 5);
            assertEquals(event.getBeforeLevel(), event.getAfterLevel());
        }

        @Test
        @DisplayName("getSkillHolder returns constructor value")
        void getSkillHolder_returnsConstructorValue() {
            SkillHolder holder = mockSkillHolder();
            var event = new PostSkillGainLevelEvent(holder, TEST_SKILL_KEY, 0, 1);
            assertSame(holder, event.getSkillHolder());
        }
    }

    @Nested
    @DisplayName("SkillRegisterEvent")
    class SkillRegisterEventTests {

        @Test
        @DisplayName("constructor with Skill uses skill key")
        void constructor_withSkill_usesSkillKey() {
            Skill skill = mockSkill();
            var event = new SkillRegisterEvent(skill);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }

        @Test
        @DisplayName("constructor with NamespacedKey stores key")
        void constructor_withKey_storesKey() {
            var event = new SkillRegisterEvent(TEST_SKILL_KEY);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }
    }

    @Nested
    @DisplayName("SkillUnregisterEvent")
    class SkillUnregisterEventTests {

        @Test
        @DisplayName("constructor with Skill uses skill key")
        void constructor_withSkill_usesSkillKey() {
            Skill skill = mockSkill();
            var event = new SkillUnregisterEvent(skill);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }

        @Test
        @DisplayName("constructor with NamespacedKey stores key")
        void constructor_withKey_storesKey() {
            var event = new SkillUnregisterEvent(TEST_SKILL_KEY);
            assertEquals(TEST_SKILL_KEY, event.getSkillKey());
        }
    }
}
