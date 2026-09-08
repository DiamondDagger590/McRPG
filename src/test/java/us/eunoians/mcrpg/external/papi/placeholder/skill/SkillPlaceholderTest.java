package us.eunoians.mcrpg.external.papi.placeholder.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder.SkillHolderData;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Skill PAPI Placeholders")
@ExtendWith(McRPGPlayerExtension.class)
class SkillPlaceholderTest extends McRPGBaseTest {

    private static final NamespacedKey SKILL_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_skill");

    private OfflinePlayer offlinePlayer(UUID uuid) {
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        return offlinePlayer;
    }

    @Nested
    @DisplayName("SkillCurrentLevelPlaceholder")
    class SkillCurrentLevelPlaceholderTests {

        @Test
        @DisplayName("returns the current level when player and skill data exist")
        void parsePlaceholder_returnsLevel_whenPlayerAndSkillExist(McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mock(SkillHolder.class);
            doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
            SkillHolderData skillData = mock(SkillHolderData.class);
            when(skillData.getCurrentLevel()).thenReturn(42);
            when(skillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(skillData));

            SkillCurrentLevelPlaceholder placeholder = new SkillCurrentLevelPlaceholder(SKILL_KEY);
            assertEquals("42", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            SkillCurrentLevelPlaceholder placeholder = new SkillCurrentLevelPlaceholder(SKILL_KEY);
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("returns null when the player has no data for the skill")
        void parsePlaceholder_returnsNull_whenSkillDataMissing(McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mock(SkillHolder.class);
            doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
            when(skillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.empty());

            SkillCurrentLevelPlaceholder placeholder = new SkillCurrentLevelPlaceholder(SKILL_KEY);
            assertNull(placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("identifier follows the expected naming pattern")
        void getIdentifier_matchesExpectedPattern() {
            SkillCurrentLevelPlaceholder placeholder = new SkillCurrentLevelPlaceholder(SKILL_KEY);
            assertEquals("test_skill_current_level", placeholder.getIdentifier());
        }
    }

    @Nested
    @DisplayName("SkillCurrentExperiencePlaceholder")
    class SkillCurrentExperiencePlaceholderTests {

        @Test
        @DisplayName("returns the current experience when player and skill data exist")
        void parsePlaceholder_returnsExperience_whenPlayerAndSkillExist(McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mock(SkillHolder.class);
            doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
            SkillHolderData skillData = mock(SkillHolderData.class);
            when(skillData.getCurrentExperience()).thenReturn(1500);
            when(skillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(skillData));

            SkillCurrentExperiencePlaceholder placeholder = new SkillCurrentExperiencePlaceholder(SKILL_KEY);
            assertEquals("1500", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            SkillCurrentExperiencePlaceholder placeholder = new SkillCurrentExperiencePlaceholder(SKILL_KEY);
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("returns null when the player has no data for the skill")
        void parsePlaceholder_returnsNull_whenSkillDataMissing(McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mock(SkillHolder.class);
            doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
            when(skillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.empty());

            SkillCurrentExperiencePlaceholder placeholder = new SkillCurrentExperiencePlaceholder(SKILL_KEY);
            assertNull(placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("identifier follows the expected naming pattern")
        void getIdentifier_matchesExpectedPattern() {
            SkillCurrentExperiencePlaceholder placeholder = new SkillCurrentExperiencePlaceholder(SKILL_KEY);
            assertEquals("test_skill_current_experience", placeholder.getIdentifier());
        }
    }

    @Nested
    @DisplayName("SkillRemainingExperiencePlaceholder")
    class SkillRemainingExperiencePlaceholderTests {

        @Test
        @DisplayName("returns the remaining experience when player and skill data exist")
        void parsePlaceholder_returnsRemainingExp_whenPlayerAndSkillExist(McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mock(SkillHolder.class);
            doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
            SkillHolderData skillData = mock(SkillHolderData.class);
            when(skillData.getExperienceForNextLevel()).thenReturn(3500);
            when(skillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(skillData));

            SkillRemainingExperiencePlaceholder placeholder = new SkillRemainingExperiencePlaceholder(SKILL_KEY);
            assertEquals("3500", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("returns null when the player is not loaded")
        void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
            SkillRemainingExperiencePlaceholder placeholder = new SkillRemainingExperiencePlaceholder(SKILL_KEY);
            assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
        }

        @Test
        @DisplayName("returns null when the player has no data for the skill")
        void parsePlaceholder_returnsNull_whenSkillDataMissing(McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mock(SkillHolder.class);
            doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
            when(skillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.empty());

            SkillRemainingExperiencePlaceholder placeholder = new SkillRemainingExperiencePlaceholder(SKILL_KEY);
            assertNull(placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
        }

        @Test
        @DisplayName("identifier follows the expected naming pattern")
        void getIdentifier_matchesExpectedPattern() {
            SkillRemainingExperiencePlaceholder placeholder = new SkillRemainingExperiencePlaceholder(SKILL_KEY);
            assertEquals("test_skill_remaining_experience_needed", placeholder.getIdentifier());
        }
    }
}
